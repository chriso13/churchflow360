package com.churchflow360.memberservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local DTO mirror of notification-service's SendNotificationRequest.
 *
 * Uses String for type and channel to avoid cross-service enum dependency.
 * Values must match notification-service enums exactly:
 *
 * type:
 *   EMAIL | SMS | BOTH
 *
 * channel:
 *   PRAYER_REQUEST | MEMBER_WELCOME | PASTORAL_MEETING |
 *   ANNOUNCEMENT   | TITHE_RECEIPT  | MANUAL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    /** Optional — membership number of recipient */
    private String recipientMembershipNumber;

    /** Required — display name of recipient */
    private String recipientName;

    /** Required for EMAIL or BOTH */
    private String recipientEmail;

    /** Required for SMS or BOTH — E.164 format preferred: +16412331066 */
    private String recipientPhone;

    /** Required — EMAIL | SMS | BOTH */
    private String type;

    /** Required — PRAYER_REQUEST | MEMBER_WELCOME | PASTORAL_MEETING | ANNOUNCEMENT | TITHE_RECEIPT | MANUAL */
    private String channel;

    /** Optional — email subject line (ignored for SMS) */
    private String subject;

    /** Required — message body (used for both email body and SMS text) */
    private String body;

    /** Optional — external reference ID (e.g. membership number, tithe ID) */
    private String referenceId;
}
