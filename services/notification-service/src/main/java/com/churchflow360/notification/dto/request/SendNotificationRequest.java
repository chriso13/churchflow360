package com.churchflow360.notification.dto.request;

import com.churchflow360.notification.model.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    private String recipientMembershipNumber;
    @NotBlank(message = "Recipient name is required") private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    @NotNull(message = "Notification type is required") private NotificationType type;
    @NotNull(message = "Channel is required") private NotificationChannel channel;
    private String subject;
    @NotBlank(message = "Message body is required") private String body;
    private String referenceId;
}
