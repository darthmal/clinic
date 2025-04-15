package com.clinicapp.backend.service.notification;

import com.clinicapp.backend.dto.notification.NotificationDTO;
import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j for logging
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j // Add Slf4j annotation
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create and send a new notification
     */
    @Transactional
    public Notification createNotification(
            User recipient,
            Notification.NotificationType type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {
        // Create the notification
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        // Save to database
        notification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        sendRealTimeNotification(notification);

        return notification;
    }

    /**
     * Send notification via WebSocket
     */
    private void sendRealTimeNotification(Notification notification) {
        // Convert to DTO to avoid circular references and lazy loading issues
        NotificationDTO notificationDTO = convertToDTO(notification);

        String destination = "/queue/notifications";
        String userEmail = notification.getRecipient().getEmail();
        log.info("Attempting to send notification DTO via WebSocket to user '{}' on destination '{}'. Payload: {}", userEmail, destination, notificationDTO);
        try {
            // Send to user's private queue
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    destination,
                    notificationDTO
            );
            log.info("Successfully sent notification DTO to user '{}'", userEmail);
        } catch (Exception e) {
            log.error("Error sending notification DTO via WebSocket to user '{}'", userEmail, e);
        }
    }

    /**
     * Get paginated notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Count unread notifications for a user
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId)
                .filter(notification -> notification.getRecipient().equals(user))
                .ifPresent(notification -> {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                });
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user, Instant.now());
    }

    /**
     * Delete old read notifications (older than 30 days)
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteOldReadNotifications() {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        notificationRepository.deleteOldReadNotifications(thirtyDaysAgo);
    }

    /**
     * Convert Notification entity to DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .recipient(notification.getRecipient().getEmail()) // Add recipient email for WebSocket routing
                .sender("system") // Set a default sender
                .build();
    }
}