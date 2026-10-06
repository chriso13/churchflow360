package com.churchflow360.pastoralservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local DTO mirror of notification-service's SendNotificationRequest.
 *
 * type:    EMAIL | SMS | BOTH
 * channel: PRAYER_REQUEST | MEMBER_WELCOME | PASTORAL_MEETING |
 *          ANNOUNCEMENT   | TITHE_RECEIPT  | MANUAL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    private String recipientMembershipNumber;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String type;
    private String channel;
    private String subject;
    private String body;
    private String referenceId;
}
