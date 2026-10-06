package com.churchflow360.pastoralservice.service;

import com.churchflow360.pastoralservice.client.NotificationClient;
import com.churchflow360.pastoralservice.client.dto.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Sends pastoral meeting notifications via notification-service Feign client.
 *
 * The PastoralMeeting entity stores membershipNumber + name but NOT email/phone.
 * We pass the membership number as recipientMembershipNumber so notification-service
 * can log it. The actual email/SMS delivery uses whatever contact info was
 * provided at call time — for pastoral meetings we send EMAIL only since
 * we don't have phone numbers on the meeting entity.
 *
 * All methods are fire-and-forget — failures never propagate to the caller.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PastoralNotificationService {

    private final NotificationClient notificationClient;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    // ── Meeting Requested ─────────────────────────────────────────────────────

    public void notifyMeetingRequested(
            String memberMembershipNumber, String memberName,
            String pastorName, String topic, String meetingId) {

        String subject = "Pastoral Meeting Request Received";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your request for a pastoral meeting has been received.\n\n" +
                "  Topic:  %s\n" +
                "  Pastor: %s\n" +
                "  Status: REQUESTED — pending confirmation\n\n" +
                "You will be notified once your meeting is confirmed.\n\n" +
                "God bless you!\n\n---\nRehoboth Pentecostal Church",
                memberName, topic, pastorName);

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(memberMembershipNumber)
                .recipientName(memberName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject(subject)
                .body(body)
                .referenceId(meetingId)
                .build());
    }

    // ── Meeting Confirmed ─────────────────────────────────────────────────────

    public void notifyMeetingConfirmed(
            String memberMembershipNumber, String memberName,
            String pastorMembershipNumber, String pastorName,
            LocalDate date, LocalTime time, String location,
            String topic, String meetingId) {

        String formattedDate = date != null ? date.format(DATE_FMT) : "TBD";
        String formattedTime = time != null ? time.format(TIME_FMT) : "TBD";
        String loc = location != null ? location : "Church Office";

        // Notify member
        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(memberMembershipNumber)
                .recipientName(memberName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject("Pastoral Meeting Confirmed")
                .body(String.format(
                        "Dear %s,\n\n" +
                        "Your pastoral meeting has been confirmed:\n\n" +
                        "  Pastor:   %s\n" +
                        "  Date:     %s\n" +
                        "  Time:     %s\n" +
                        "  Location: %s\n" +
                        "  Topic:    %s\n\n" +
                        "Please arrive on time.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                        memberName, pastorName, formattedDate, formattedTime, loc, topic))
                .referenceId(meetingId)
                .build());

        // Notify pastor
        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(pastorMembershipNumber)
                .recipientName(pastorName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject("Meeting Confirmed — " + memberName)
                .body(String.format(
                        "Dear Pastor %s,\n\n" +
                        "A meeting has been added to your schedule:\n\n" +
                        "  Member:   %s\n" +
                        "  Date:     %s\n" +
                        "  Time:     %s\n" +
                        "  Location: %s\n" +
                        "  Topic:    %s\n\n" +
                        "God bless your ministry!\n\n---\nRehoboth Pentecostal Church",
                        pastorName, memberName, formattedDate, formattedTime, loc, topic))
                .referenceId(meetingId)
                .build());
    }

    // ── Meeting Rescheduled ───────────────────────────────────────────────────

    public void notifyMeetingRescheduled(
            String memberMembershipNumber, String memberName,
            String pastorName, LocalDate newDate, LocalTime newTime,
            String location, String reason, String meetingId) {

        String formattedDate = newDate != null ? newDate.format(DATE_FMT) : "TBD";
        String formattedTime = newTime != null ? newTime.format(TIME_FMT) : "TBD";

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(memberMembershipNumber)
                .recipientName(memberName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject("Pastoral Meeting Rescheduled")
                .body(String.format(
                        "Dear %s,\n\n" +
                        "Your pastoral meeting has been rescheduled:\n\n" +
                        "  Pastor:   %s\n" +
                        "  New Date: %s\n" +
                        "  New Time: %s\n" +
                        "  Location: %s\n" +
                        "  Reason:   %s\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                        memberName, pastorName, formattedDate, formattedTime,
                        location != null ? location : "Church Office",
                        reason != null ? reason : "Not specified"))
                .referenceId(meetingId)
                .build());
    }

    // ── Meeting Cancelled ─────────────────────────────────────────────────────

    public void notifyMeetingCancelled(
            String memberMembershipNumber, String memberName,
            String pastorName, String reason, String meetingId) {

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(memberMembershipNumber)
                .recipientName(memberName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject("Pastoral Meeting Cancelled")
                .body(String.format(
                        "Dear %s,\n\n" +
                        "Your pastoral meeting with Pastor %s has been cancelled.\n\n" +
                        "  Reason: %s\n\n" +
                        "Please contact the church office to reschedule.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                        memberName, pastorName,
                        reason != null ? reason : "Not specified"))
                .referenceId(meetingId)
                .build());
    }

    // ── Meeting Completed ─────────────────────────────────────────────────────

    public void notifyMeetingCompleted(
            String memberMembershipNumber, String memberName,
            String pastorName, String meetingId) {

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(memberMembershipNumber)
                .recipientName(memberName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject("Pastoral Meeting Completed")
                .body(String.format(
                        "Dear %s,\n\n" +
                        "Your pastoral meeting with Pastor %s has been completed.\n\n" +
                        "Thank you for taking the time to meet. " +
                        "We pray God continues to guide you.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                        memberName, pastorName))
                .referenceId(meetingId)
                .build());
    }

    // ── Meeting No-Show ───────────────────────────────────────────────────────

    public void notifyMeetingNoShow(
            String memberMembershipNumber, String memberName,
            String pastorName, LocalDate scheduledDate, String meetingId) {

        String formattedDate = scheduledDate != null ? scheduledDate.format(DATE_FMT) : "your scheduled date";

        send(SendNotificationRequest.builder()
                .recipientMembershipNumber(memberMembershipNumber)
                .recipientName(memberName)
                .type("EMAIL")
                .channel("PASTORAL_MEETING")
                .subject("Pastoral Meeting — Follow-up Required")
                .body(String.format(
                        "Dear %s,\n\n" +
                        "We noticed you were unable to attend your pastoral meeting " +
                        "with Pastor %s scheduled for %s.\n\n" +
                        "Please contact the church office to reschedule at your earliest convenience.\n\n" +
                        "God bless you!\n\n---\nRehoboth Pentecostal Church",
                        memberName, pastorName, formattedDate))
                .referenceId(meetingId)
                .build());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void send(SendNotificationRequest req) {
        try {
            notificationClient.send(req);
            log.info("Notification dispatched → {} channel={} type={}",
                    req.getRecipientName(), req.getChannel(), req.getType());
        } catch (Exception e) {
            log.warn("Failed to dispatch notification to {} ({}): {}",
                    req.getRecipientName(), req.getChannel(), e.getMessage());
        }
    }
}
