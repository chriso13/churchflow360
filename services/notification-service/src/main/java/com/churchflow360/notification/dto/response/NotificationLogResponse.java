package com.churchflow360.notification.dto.response;

import com.churchflow360.notification.model.*;
import lombok.Data;
import java.time.*;
import java.util.UUID;

@Data
public class NotificationLogResponse {
    private UUID id;
    private String recipientMembershipNumber;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String body;
    private NotificationStatus emailStatus;
    private NotificationStatus smsStatus;
    private String emailError;
    private String smsError;
    private String referenceId;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
