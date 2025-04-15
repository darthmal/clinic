package com.clinicapp.backend.dto.notification;

import com.clinicapp.backend.model.notification.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private Instant createdAt;
    private boolean read;
    private String referenceType;
    private Long referenceId;
    private String sender; // NEW: sender's email
    private String recipient; // NEW: recipient's email
}