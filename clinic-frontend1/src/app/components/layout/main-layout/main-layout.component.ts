import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../services/auth/auth.service';
import { WebsocketService } from '../../../services/websocket/websocket.service';
import { NotificationService } from '../../../services/notification/notification.service';
import { NotificationDTO } from '../../../models/notification.dto';
import { UserRole } from '../../../models/user.dto';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [ CommonModule, RouterOutlet, RouterLink, RouterLinkActive ],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  currentUserRole: UserRole | null = null;
  notifications: NotificationDTO[] = [];
  unreadNotificationCount = 0;
  private notificationSubscription: Subscription | null = null;
  showNotificationsPanel = false;
  currentPage = 1;
  readonly pageSize = 10;
  totalNotifications = 0;
  loading = false;

  
  constructor(
    private authService: AuthService,
    private websocketService: WebsocketService,
    private notificationService: NotificationService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUserRole = this.authService.getCurrentUserRole();
    this.loadAllNotifications();
    this.subscribeToNotificationMessages();
  }

  ngOnDestroy(): void {
    this.notificationSubscription?.unsubscribe();
  }

  private loadAllNotifications(page: number = 1): void {
    this.loading = true;
    // Load all notifications with pagination
    this.notificationService.getUserNotifications(page, this.pageSize).subscribe({
      next: (response) => {
        if (page === 1) {
          this.notifications = response.content;
        } else {
          this.notifications = [...this.notifications, ...response.content];
        }
        this.totalNotifications = response.totalElements;
        this.currentPage = page;
        this.updateUnreadCount();
        this.loading = false;
      },
      error: (error) => {
        console.error('[MainLayoutComponent] Error loading notifications:', error);
        this.loading = false;
      }
    });
  }

  private updateUnreadCount(): void {
    this.notificationService.getUnreadNotificationCount().subscribe({
      next: (count) => {
        this.unreadNotificationCount = count;
      },
      error: (error) => {
        console.error('[MainLayoutComponent] Error getting unread count:', error);
      }
    });
  }

  loadMoreNotifications(): void {
    if (!this.loading && this.notifications.length < this.totalNotifications) {
      this.loadAllNotifications(this.currentPage + 1);
    }
  }

  onNotificationClick(notification: NotificationDTO): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.read = true;
          this.updateUnreadCount();
        },
        error: (error) => {
          console.error('[MainLayoutComponent] Error marking notification as read:', error);
        }
      });
    }
  }

  subscribeToNotificationMessages(): void {
    this.notificationSubscription?.unsubscribe();
    console.log('[MainLayoutComponent] Subscribing to notifications$...');
    this.notificationSubscription = this.websocketService.notifications$.subscribe({
      next: (notification: NotificationDTO) => {
        console.log('[MainLayoutComponent] Received notification (realtime):', notification);
        this.notifications.unshift(notification);
        this.unreadNotificationCount++;
        this.totalNotifications++;
        this.showNotificationToast(notification);
      },
      error: (error: Error) => {
        console.error('[MainLayoutComponent] Error receiving notification:', error);
      }
    });
  }

  showNotificationToast(notification: NotificationDTO): void {
    console.log('[MainLayoutComponent] Showing toast for:', notification.title);
    this.toastr.info(notification.message, notification.title || 'New Notification', {
      closeButton: true,
      progressBar: true,
    });
  }

  viewNotifications(): void {
    this.showNotificationsPanel = !this.showNotificationsPanel;
  }

  closeNotificationsPanel(): void {
    this.showNotificationsPanel = false;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onScroll(event: any): void {
    const element = event.target;
    const nearBottom = element.scrollHeight - element.scrollTop <= element.clientHeight * 1.5;
    
    if (nearBottom) {
      this.loadMoreNotifications();
    }
  }

  markAllNotificationsAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(notification => {
          notification.read = true;
        });
        this.unreadNotificationCount = 0;
      },
      error: (error) => {
        console.error('[MainLayoutComponent] Error marking all notifications as read:', error);
      }
    });
  }
}
