package com.clinicapp.backend.model.notification;

import com.clinicapp.backend.model.security.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(nullable = false)
    private boolean read;

    // Optional: Link to related entity (e.g., appointment, prescription)
    private Long referenceId;
    
    @Column(nullable = false)
    private String referenceType; // e.g., "APPOINTMENT", "PRESCRIPTION", "MESSAGE"

    public enum NotificationType {
        APPOINTMENT_REMINDER,
        NEW_MESSAGE,
        PRESCRIPTION_READY,
        INVOICE_GENERATED,
        APPOINTMENT_CANCELLED,
        APPOINTMENT_MODIFIED,
        SYSTEM_NOTIFICATION
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        read = false;
    }

    public void markAsRead() {
        this.read = true;
        this.readAt = Instant.now();
    }
}