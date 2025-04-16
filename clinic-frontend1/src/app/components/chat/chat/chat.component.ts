import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Import FormsModule for ngModel
import { Subscription } from 'rxjs';
import { WebsocketService } from '../../../services/websocket/websocket.service';
import { AuthService } from '../../../services/auth/auth.service';
import { ChatMessage } from '../../../models/chat-message.dto';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule], // Add FormsModule
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messageContainer') private messageContainer!: ElementRef; // To scroll to bottom

  messages: ChatMessage[] = [];
  newMessage: string = '';
  currentUserEmail: string | null = null;
  recipientEmail: string = ''; // For sending private messages
  connectionState: string = 'DISCONNECTED';

  private subscriptions: Subscription = new Subscription();

  constructor(
    private websocketService: WebsocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserEmail = this.authService.getCurrentUserValue()?.email ?? null;

    // Subscribe to connection state changes
    this.subscriptions.add(
      this.websocketService.connectionState$.subscribe(state => {
        this.connectionState = state;
        if (state === 'CONNECTED') {
          console.log('Chat component aware of WS connection.');
        }
      })
    );

    // Subscribe to public messages
    this.subscriptions.add(
      this.websocketService.publicChatMessages$.subscribe(message => {
        this.messages.push(message);
        this.scrollToBottom(); // Scroll after adding message
      })
    );

    // Subscribe to private messages
    this.subscriptions.add(
      this.websocketService.privateChatMessages$.subscribe(message => {
         // Add visual distinction for private messages if needed
         message.content = `(Private) ${message.content}`;
        this.messages.push(message);
        this.scrollToBottom(); // Scroll after adding message
      })
    );

     // Ensure connection is attempted if not already connected
     if (this.connectionState !== 'CONNECTED' && this.connectionState !== 'CONNECTING') {
        this.websocketService.connect();
     }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    // Optional: Disconnect WebSocket when component is destroyed if desired,
    // but the service currently handles disconnect on logout.
    // this.websocketService.disconnect();
  }

   ngAfterViewChecked(): void {
     // Scroll to bottom after view updates (e.g., when messages array changes)
     // Be cautious with frequent calls; might need optimization if performance issues arise
     this.scrollToBottom();
   }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.currentUserEmail) {
      return;
    }

    const message: ChatMessage = {
      sender: this.currentUserEmail,
      content: this.newMessage,
      type: 'CHAT'
    };

    if (this.recipientEmail.trim()) {
      // --- Send Private Message ---
      message.recipient = this.recipientEmail.trim();
      this.websocketService.sendMessage('/app/chat.private', message);
      // Add message locally immediately for sender (optional, backend sends it back too)
      // this.messages.push({ ...message, content: `(Private to ${message.recipient}) ${message.content}` });
    } else {
      // --- Send Public Message ---
      this.websocketService.sendMessage('/app/chat.public', message);
       // Add message locally immediately for sender (optional, backend sends it back too)
       // this.messages.push(message);
    }

    this.newMessage = ''; // Clear input field
    this.scrollToBottom(); // Scroll after sending
  }

   scrollToBottom(): void {
     try {
       if (this.messageContainer?.nativeElement) {
         this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
       }
     } catch (err) {
       console.error('Could not scroll to bottom:', err);
     }
   }
}
