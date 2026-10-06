package com.churchflow360.notification.listener;

import com.churchflow360.notification.config.RabbitMQConfig;
import com.churchflow360.notification.model.*;
import com.churchflow360.notification.service.NotificationService;
import com.churchflow360.notification.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Listens to RabbitMQ events and dispatches notifications.
 *
 * Each method receives a Map payload from the publishing service.
 * Add corresponding RabbitMQ publish calls in each microservice when events occur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final TemplateService     templateService;

    // ── Member Welcome ────────────────────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.Q_WELCOME)
    public void onMemberRegistered(Map<String, Object> event) {
        log.info("Member registered event received: {}", event.get("membershipNumber"));
        try {
            String name  = str(event, "fullName");
            String email = str(event, "email");
            String phone = str(event, "phone");
            String mn    = str(event, "membershipNumber");

            notificationService.sendBoth(
                    email, phone, name,
                    "Welcome to Rehoboth Pentecostal Church!",
                    templateService.welcomeEmail(name),
                    NotificationChannel.MEMBER_WELCOME, mn
            );
        } catch (Exception e) {
            log.error("Failed to process member welcome notification: {}", e.getMessage());
        }
    }

    // ── Prayer Request ────────────────────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.Q_PRAYER)
    public void onPrayerRequestCreated(Map<String, Object> event) {
        log.info("Prayer request event received: {}", event.get("requestId"));
        try {
            // Notify the assigned pastor if one is set
            String pastorEmail = str(event, "assignedPastorEmail");
            String pastorName  = str(event, "assignedPastorName");
            String pastorPhone = str(event, "assignedPastorPhone");

            if (pastorEmail != null && !pastorEmail.isBlank()) {
                String requester = str(event, "requesterName");
                String title     = str(event, "title");
                String text      = str(event, "requestText");
                String refId     = str(event, "requestId");

                notificationService.sendBoth(
                        pastorEmail, pastorPhone, pastorName,
                        "New Prayer Request: " + title,
                        templateService.prayerRequestEmail(pastorName, requester, title, text),
                        NotificationChannel.PRAYER_REQUEST, refId
                );
            }
        } catch (Exception e) {
            log.error("Failed to process prayer request notification: {}", e.getMessage());
        }
    }

    // ── Pastoral Meeting ──────────────────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.Q_PASTORAL)
    public void onPastoralMeetingScheduled(Map<String, Object> event) {
        log.info("Pastoral meeting event received: {}", event.get("meetingId"));
        try {
            String memberName  = str(event, "memberName");
            String memberEmail = str(event, "memberEmail");
            String memberPhone = str(event, "memberPhone");
            String pastorName  = str(event, "pastorName");
            String date        = str(event, "preferredDate");
            String time        = str(event, "preferredTime");
            String topic       = str(event, "topic");
            String refId       = str(event, "meetingId");

            notificationService.sendBoth(
                    memberEmail, memberPhone, memberName,
                    "Pastoral Meeting Scheduled",
                    templateService.pastoralMeetingEmail(memberName, pastorName, date, time, topic),
                    NotificationChannel.PASTORAL_MEETING, refId
            );
        } catch (Exception e) {
            log.error("Failed to process pastoral meeting notification: {}", e.getMessage());
        }
    }

    // ── Announcement Published ────────────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.Q_ANNOUNCE)
    public void onAnnouncementPublished(Map<String, Object> event) {
        log.info("Announcement event received: {}", event.get("announcementId"));
        try {
            // For broadcast announcements, the publishing service should
            // include a list of recipient emails/phones
            String recipientEmail = str(event, "recipientEmail");
            String recipientName  = str(event, "recipientName");
            String recipientPhone = str(event, "recipientPhone");
            String title   = str(event, "title");
            String content = str(event, "content");
            String refId   = str(event, "announcementId");

            notificationService.sendBoth(
                    recipientEmail, recipientPhone, recipientName,
                    "Church Announcement: " + title,
                    templateService.announcementEmail(recipientName, title, content),
                    NotificationChannel.ANNOUNCEMENT, refId
            );
        } catch (Exception e) {
            log.error("Failed to process announcement notification: {}", e.getMessage());
        }
    }

    // ── Tithe Receipt ─────────────────────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.Q_TITHE)
    public void onTitheRecorded(Map<String, Object> event) {
        log.info("Tithe receipt event received: {}", event.get("titheId"));
        try {
            String memberName  = str(event, "memberName");
            String memberEmail = str(event, "memberEmail");
            String memberPhone = str(event, "memberPhone");
            String amount      = str(event, "amount");
            String month       = str(event, "titheMonth");
            String year        = str(event, "titheYear");
            String refId       = str(event, "titheId");

            notificationService.sendBoth(
                    memberEmail, memberPhone, memberName,
                    "Tithe Receipt — " + month + " " + year,
                    templateService.titheReceiptEmail(memberName, amount, month, year, refId),
                    NotificationChannel.TITHE_RECEIPT, refId
            );
        } catch (Exception e) {
            log.error("Failed to process tithe receipt notification: {}", e.getMessage());
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
