import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject, filter, first, takeUntil } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { ChatMessage } from '../../models/chat-message.dto';
import { NotificationDTO } from '../../models/notification.dto'; // Correct import path

// Type guard for NotificationDTO
function isNotificationDTO(message: any): message is NotificationDTO {
  // Add more checks if needed based on NotificationDTO structure
  return message && 
         typeof message.title === 'string' && 
         typeof message.message === 'string' &&
         (message.id === undefined || typeof message.id === 'number'); // Make id optional
}

// Type guard for ChatMessage
function isChatMessage(message: any): message is ChatMessage {
   return message && typeof message.sender === 'string' && typeof message.content === 'string';
}

@Injectable({
  providedIn: 'root'
})
export class WebsocketService implements OnDestroy {
  private stompClient?: Client;
  private connectionState = new BehaviorSubject<'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR'>('DISCONNECTED');
  private subscriptions: Map<string, StompSubscription> = new Map();
  private destroy$ = new Subject<void>();

  // Subjects to emit received messages
  private publicChatMessages = new Subject<ChatMessage>();
  private privateChatMessages = new Subject<ChatMessage>();
  private notifications = new Subject<NotificationDTO>(); // Single subject for notifications

  // Public observables
  public connectionState$: Observable<typeof this.connectionState.value> = this.connectionState.asObservable();
  public publicChatMessages$: Observable<ChatMessage> = this.publicChatMessages.asObservable();
  public privateChatMessages$: Observable<ChatMessage> = this.privateChatMessages.asObservable();
  public notifications$: Observable<NotificationDTO> = this.notifications.asObservable(); // Single observable for notifications

  constructor(private authService: AuthService) {
    console.log('WebSocket service constructed');
    this.authService.isLoggedIn$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(loggedIn => {
      console.log('Auth state changed. LoggedIn:', loggedIn);
      if (loggedIn && !this.isConnected()) {
        console.log('User is logged in and not connected, attempting connection...');
        this.connect();
      } else if (!loggedIn && this.isConnected()) {
        console.log('User logged out, disconnecting...');
        this.disconnect();
      }
    });

    // Check initial auth state
     this.authService.isLoggedIn$.pipe(first()).subscribe(initialLoggedIn => {
       if (initialLoggedIn && !this.isConnected()) {
         console.log('User already logged in on service init, connecting...');
         this.connect();
       }
     });
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private isConnected(): boolean {
    return this.stompClient?.active ?? false;
  }

  connect(): void {
    if (this.isConnected() || this.connectionState.value === 'CONNECTING') {
      console.log('WebSocket already connected or connecting.');
      return;
    }

    const token = this.authService.getToken();
    const user = this.authService.getCurrentUserValue();

    if (!token || !user) {
      console.error('WebSocket connection failed: Token or user not found.');
      this.connectionState.next('ERROR');
      return;
    }

    console.log(`Attempting WebSocket connection for user: ${user.email}`);
    this.connectionState.next('CONNECTING');
    const sockJsUrl = `${environment.apiUrl.replace('/api/v1', '')}/ws`;
    console.log('Connecting to WebSocket URL:', sockJsUrl);

    this.stompClient = new Client({
      webSocketFactory: () => new (SockJS as any)(sockJsUrl),
      connectHeaders: { Authorization: `Bearer ${token}` },
      debug: (str) => { console.log('STOMP Debug:', str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('WebSocket Connected:', frame);
      this.connectionState.next('CONNECTED');
      // Subscribe to topics/queues
      this.subscribeToTopic('/topic/public', this.publicChatMessages);
      this.subscribeToTopic('/user/queue/private', this.privateChatMessages);
      this.subscribeToTopic('/user/queue/notifications', this.notifications); // Single subscription to notifications
      this.subscribeToTopic('/topic/notifications', this.notifications); // Subscribe to broadcast notifications
      // Send join message
      this.sendMessage('/app/chat.join', { sender: user.email, type: 'JOIN' });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error:', frame.headers['message'], 'Body:', frame.body);
      this.connectionState.next('ERROR');
    };
    this.stompClient.onWebSocketError = (error) => {
       console.error('WebSocket Error:', error);
       this.connectionState.next('ERROR');
    };
     this.stompClient.onDisconnect = () => {
        console.log('WebSocket Disconnected');
        this.connectionState.next('DISCONNECTED');
        this.subscriptions.clear();
     };

    this.stompClient.activate();
  }

  disconnect(): void {
     if (this.stompClient?.active) {
       const user = this.authService.getCurrentUserValue();
       if (user) { this.sendMessage('/app/chat.leave', { sender: user.email, type: 'LEAVE' }); }
       this.stompClient.deactivate().then(() => console.log('WebSocket deactivated.'))
         .catch(e => console.error('Error during WebSocket deactivation:', e));
     }
     this.connectionState.next('DISCONNECTED');
     this.subscriptions.clear();
     this.stompClient = undefined;
  }

  private subscribeToTopic(topic: string, subject: Subject<any>): void {
    if (!this.stompClient?.active) {
      console.error(`Cannot subscribe to ${topic}: STOMP client not active.`);
      this.connectionState$.pipe(filter(state => state === 'CONNECTED'), first(), takeUntil(this.destroy$))
        .subscribe(() => this.subscribeToTopic(topic, subject));
      return;
    }
    if (this.subscriptions.has(topic)) { return; } // Already subscribed

    console.log(`Subscribing to ${topic}...`);
    try {
      const subscription = this.stompClient.subscribe(topic, (message: IMessage) => {
        console.log(`Raw message received on ${topic}:`, message.body); // Log raw message
        try {
          const body = JSON.parse(message.body);
          console.log(`Parsed message body on ${topic}:`, body); // Log parsed body

          // Route based on topic
          if (topic.includes('/queue/notifications')) {
            if (isNotificationDTO(body)) {
              console.log(`[WebsocketService] Emitting NotificationDTO on notifications$`);
              subject.next(body); // Emit NotificationDTO
            } else {
              console.warn(`Received non-NotificationDTO message on notification topic ${topic}:`, body);
            }
          } else if (topic.includes('/queue/private') || topic.includes('/topic/public')) {
            if (isChatMessage(body)) {
               console.log(`[WebsocketService] Emitting ChatMessage on ${topic.includes('/private') ? 'private' : 'public'}ChatMessages$`);
               subject.next(body); // Emit ChatMessage
            } else {
               console.warn(`Received non-ChatMessage message on chat topic ${topic}:`, body);
            }
          } else {
             console.warn(`Received message on unhandled topic ${topic}:`, body);
          }
        } catch (parseError) {
          console.error(`Could not parse JSON message on ${topic}: ${message.body}`, parseError);
        }
      });
      this.subscriptions.set(topic, subscription);
      console.log(`Successfully subscribed to ${topic}.`);
    } catch (error) {
      console.error(`Error subscribing to ${topic}:`, error);
    }
  }

  // Unsubscribe logic remains the same...
  unsubscribeFromTopic(topic: string): void {
    const subscription = this.subscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(topic);
      console.log(`Unsubscribed from ${topic}`);
    } else {
       console.warn(`No active subscription found for topic: ${topic}`);
    }
  }

  // Send message logic remains the same...
  sendMessage(destination: string, body: any): void {
    if (this.stompClient?.active) {
      this.stompClient.publish({ destination: destination, body: JSON.stringify(body) });
      console.log(`Message sent to ${destination}:`, body);
    } else {
      console.error(`Cannot send message to ${destination}: STOMP client not active.`);
    }
  }

  /**
   * Send a notification to a specific user (private notification)
   */
  sendNotification(notification: NotificationDTO) {
    this.sendMessage('/app/notification.private', notification);
  }

  /**
   * Broadcast a notification to all users (if needed)
   */
  broadcastNotification(notification: NotificationDTO) {
    this.sendMessage('/app/notification.broadcast', notification);
  }

  /**
   * Debug notificationMessages$ subscription
   * Call this method to debug notification messages
   */
  debugNotifications(): void {
    this.notifications$.subscribe((message: any) => {
      try {
        console.log('Raw message received from notifications$:', message);
        console.log('Message type:', typeof message);
        if (typeof message === 'string') {
          console.log('String message received:', message);
        } else if (typeof message === 'object') {
          console.log('Object message received:', message);
          if (isNotificationDTO(message)) {
            console.log('NotificationDTO message received:', message);
          } else {
            console.log('Non-NotificationDTO object message received:', message);
          }
        } else {
          console.log('Unknown message type received:', message);
        }
      } catch (error) {
        console.error('Error processing notification message:', error);
      }
    });
  }

  /**
   * Send a test notification via WebSocket
   */
  sendTestNotification(): void {
    if (!this.stompClient?.active) {
      console.error('Cannot send test notification: STOMP client not active');
      return;
    }

    const user = this.authService.getCurrentUserValue();
    if (!user) {
      console.error('Cannot send test notification: No user logged in');
      return;
    }

    const testNotification = {
      title: 'Test Notification',
      message: 'This is a test notification sent at ' + new Date().toLocaleTimeString(),
      sender: user.email,
      recipient: user.email,
      createdAt: new Date().toISOString(),
      read: false
    };

    console.log('Sending test notification:', testNotification);
    this.stompClient.publish({
      destination: '/app/notification.private',
      body: JSON.stringify(testNotification)
    });
  }
}
