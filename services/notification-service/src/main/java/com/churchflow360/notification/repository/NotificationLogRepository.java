package com.churchflow360.notification.repository;

import com.churchflow360.notification.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    Page<NotificationLog> findByRecipientMembershipNumberOrderByCreatedAtDesc(String mn, Pageable p);
    Page<NotificationLog> findByChannelOrderByCreatedAtDesc(NotificationChannel channel, Pageable p);
    Page<NotificationLog> findByEmailStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable p);
    long countByEmailStatus(NotificationStatus status);
    long countBySmsStatus(NotificationStatus status);
    long countByChannel(NotificationChannel channel);
}
