package com.clinicapp.backend.controller.notification;

import com.clinicapp.backend.dto.notification.NotificationDTO;
import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

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