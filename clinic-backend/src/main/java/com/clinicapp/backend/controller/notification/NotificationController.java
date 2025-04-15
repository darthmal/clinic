package com.clinicapp.backend.controller.notification;

import com.clinicapp.backend.dto.notification.NotificationDTO;
import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get paginated notifications for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        Page<Notification> notificationPage = notificationService.getUserNotifications(user, pageable);
        Page<NotificationDTO> dtoPage = notificationPage.map(this::convertToDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get unread notifications for the authenticated user.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal User user
    ) {
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get the count of unread notifications for the authenticated user.
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @AuthenticationPrincipal User user
    ) {
        long count = notificationService.countUnreadNotifications(user);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark a specific notification as read.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read for the authenticated user.
     */
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @AuthenticationPrincipal User user
    ) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    /**
     * WebSocket: Send a notification to a specific user (private notification)
     * Client sends to: /app/notification.private
     * Backend delivers to: /user/{recipient}/queue/notifications
     */
    @MessageMapping("/notification.private")
    public void sendPrivateNotification(
            @Payload NotificationDTO notificationDTO,
            Principal principal
    ) {
        if (notificationDTO.getSender() == null) {
            notificationDTO.setSender(principal.getName());
        }
        if (notificationDTO.getCreatedAt() == null) {
            notificationDTO.setCreatedAt(Instant.now());
        }
        if (notificationDTO.getRecipient() == null) {
            throw new IllegalArgumentException("Recipient required");
        }
        // Send to recipient
        messagingTemplate.convertAndSendToUser(
                notificationDTO.getRecipient(),
                "/queue/notifications",
                notificationDTO
        );
        // Optionally, send to sender as well
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/notifications",
                notificationDTO
        );
    }

    /**
     * WebSocket: Broadcast a notification to all users (if needed)
     * Client sends to: /app/notification.broadcast
     * Backend delivers to: /topic/notifications
     */
    @MessageMapping("/notification.broadcast")
    public void broadcastNotification(@Payload NotificationDTO notificationDTO, Principal principal) {
        if (notificationDTO.getSender() == null) {
            notificationDTO.setSender(principal.getName());
        }
        if (notificationDTO.getCreatedAt() == null) {
            notificationDTO.setCreatedAt(Instant.now());
        }
        messagingTemplate.convertAndSend("/topic/notifications", notificationDTO);
    }

    /**
     * Test endpoint to send a notification to a specific user
     * This is for testing only and should be removed in production
     */
    @PostMapping("/test-notification")
    public ResponseEntity<Void> sendTestNotification(
            @RequestParam String recipientEmail,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            // Create a test notification DTO
            NotificationDTO testNotification = NotificationDTO.builder()
                    .id(0L)  // Will be ignored when saving
                    .type(Notification.NotificationType.SYSTEM_NOTIFICATION)
                    .title("Test Notification")
                    .message("This is a test notification sent at " + Instant.now())
                    .createdAt(Instant.now())
                    .read(false)
                    .sender(currentUser.getEmail())
                    .recipient(recipientEmail)
                    .build();
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    "/queue/notifications",
                    testNotification
            );
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Convert Notification entity to DTO.
     * (Could be moved to a dedicated mapper class)
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
                .build();
    }
}