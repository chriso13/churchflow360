package com.churchflow360.memberservice.service;

import com.churchflow360.memberservice.client.NotificationClient;
import com.churchflow360.memberservice.client.dto.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Builds and dispatches notifications to notification-service via Feign.
 *
 * All methods are fire-and-forget — failures are logged but never
 * propagate to the caller. Member operations must not fail because
 * a notification couldn't be sent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberNotificationService {

    private final NotificationClient notificationClient;

    // ── Welcome ───────────────────────────────────────────────────────────────

    public void sendWelcome(String email, String phone,
                            String fullName, String membershipNumber) {
        String subject = "Welcome to Rehoboth Pentecostal Church!";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Welcome to 360 Church Flow — the digital home of Rehoboth Pentecostal Church!\n\n" +
                        "Your membership has been confirmed.\n" +
                        "Membership Number: %s\n\n" +
                        "You can now access our online portal to view upcoming events, submit prayer " +
                        "requests, manage your tithe records, and connect with your small group.\n\n" +
                        "We are blessed to have you in our congregation.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                fullName, membershipNumber);

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(membershipNumber)
                .recipientName(fullName)
                .recipientEmail(email)
                .recipientPhone(phone)
                .type(resolveType(email, phone))
                .channel("MEMBER_WELCOME")
                .subject(subject)
                .body(body)
                .referenceId(membershipNumber)
                .build());
    }

    // ── Profile updated ───────────────────────────────────────────────────────

    public void sendProfileUpdated(String email, String phone,
                                   String fullName, String membershipNumber) {
        String subject = "Your Profile Has Been Updated";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your member profile (Membership No: %s) has been successfully updated.\n\n" +
                        "If you did not request this change, please contact the church office immediately.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                fullName, membershipNumber);

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(membershipNumber)
                .recipientName(fullName)
                .recipientEmail(email)
                .recipientPhone(phone)
                .type(resolveType(email, phone))
                .channel("MEMBER_PROFILE_UPDATED")
                .subject(subject)
                .body(body)
                .referenceId(membershipNumber)
                .build());
    }

    // ── Status change ─────────────────────────────────────────────────────────

    public void sendStatusChange(String email, String phone,
                                 String fullName, String membershipNumber,
                                 String newStatus) {
        String subject = "Your Membership Status Has Changed";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your membership status has been updated to: %s\n" +
                        "Membership Number: %s\n\n" +
                        "Please contact the church office if you have any questions.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                fullName, newStatus, membershipNumber);

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(membershipNumber)
                .recipientName(fullName)
                .recipientEmail(email)
                .recipientPhone(phone)
                .type(resolveType(email, phone))
                .channel("MEMBER_STATUS_CHANGE")
                .subject(subject)
                .body(body)
                .referenceId(membershipNumber)
                .build());
    }

    // ── Account deleted ───────────────────────────────────────────────────────

    public void sendAccountDeleted(String email, String phone,
                                   String fullName, String membershipNumber) {
        String subject = "Your Church Record Has Been Removed";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your member record (Membership No: %s) has been removed from our system.\n\n" +
                        "If you believe this was done in error, please contact the church office.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                fullName, membershipNumber);

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(membershipNumber)
                .recipientName(fullName)
                .recipientEmail(email)
                .recipientPhone(phone)
                .type(resolveType(email, phone))
                .channel("MEMBER_DELETED")
                .subject(subject)
                .body(body)
                .referenceId(membershipNumber)
                .build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void send(SendNotificationRequest req) {
        try {
            notificationClient.send(req);
            log.info("Notification dispatched → {} channel={} type={}",
                    req.getRecipientName(), req.getChannel(), req.getType());
        } catch (Exception e) {
            // Never propagate — member operation must succeed regardless
            log.warn("Failed to dispatch notification to {} ({}): {}",
                    req.getRecipientName(), req.getChannel(), e.getMessage());
        }
    }

    /**
     * Use BOTH if phone is present, EMAIL if only email, SMS if only phone.
     */
    private String resolveType(String email, String phone) {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (hasEmail && hasPhone) return "BOTH";
        if (hasEmail)             return "EMAIL";
        return "SMS";
    }
}
