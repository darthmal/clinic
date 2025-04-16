// Matches com.clinicapp.backend.dto.notification.NotificationDTO

// Ensure this matches the NotificationType enum in the backend
export type NotificationType =
  | 'APPOINTMENT_REMINDER'
  | 'NEW_MESSAGE'
  | 'PRESCRIPTION_READY'
  | 'INVOICE_GENERATED'
  | 'APPOINTMENT_CANCELLED'
  | 'APPOINTMENT_MODIFIED'
  | 'SYSTEM_NOTIFICATION';

export interface NotificationDTO {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  createdAt: string; // ISO string format
  read: boolean;
  referenceType?: string; // e.g., "APPOINTMENT", "PRESCRIPTION", "MESSAGE"
  referenceId?: number;
  sender?: string; // who sent the notification
  recipient?: string; // who should receive it
}