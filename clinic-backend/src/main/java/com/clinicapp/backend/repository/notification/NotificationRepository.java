package com.clinicapp.backend.repository.notification;

import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
    
    // Find unread notifications for a user
    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);
    
    // Count unread notifications for a user
    long countByRecipientAndReadFalse(User recipient);
    
    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = ?2 WHERE n.recipient = ?1 AND n.read = false")
    void markAllAsRead(User recipient, Instant readAt);
    
    // Find notifications by type for a user
    Page<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(
            User recipient, 
            Notification.NotificationType type, 
            Pageable pageable
    );
    
    // Delete old read notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.read = true AND n.createdAt < ?1")
    void deleteOldReadNotifications(Instant before);
}