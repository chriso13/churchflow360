package com.churchflow360.notification.service;

import com.churchflow360.notification.dto.request.SendNotificationRequest;
import com.churchflow360.notification.dto.response.NotificationLogResponse;
import com.churchflow360.notification.model.*;
import com.churchflow360.notification.repository.NotificationLogRepository;
import com.churchflow360.notification.service.email.BrevoEmailService;
import com.churchflow360.notification.service.sms.TwilioSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final BrevoEmailService         emailService;
    private final TwilioSmsService          smsService;
    private final NotificationLogRepository logRepository;

    @Transactional
    public NotificationLogResponse send(SendNotificationRequest req) {
        NotificationLog n = NotificationLog.builder()
                .recipientMembershipNumber(req.getRecipientMembershipNumber())
                .recipientName(req.getRecipientName())
                .recipientEmail(req.getRecipientEmail())
                .recipientPhone(req.getRecipientPhone())
                .type(req.getType())
                .channel(req.getChannel())
                .subject(req.getSubject())
                .body(req.getBody())
                .referenceId(req.getReferenceId())
                .build();

        boolean sendEmail = req.getType() == NotificationType.EMAIL
                || req.getType() == NotificationType.BOTH;
        boolean sendSms   = req.getType() == NotificationType.SMS
                || req.getType() == NotificationType.BOTH;

        // ── Email ──────────────────────────────────────────────────────────
        if (sendEmail) {
            BrevoEmailService.EmailResult r = emailService.sendEmail(
                    req.getRecipientEmail(), req.getRecipientName(),
                    req.getSubject(), req.getBody());

            if (r.isSent())         n.setEmailStatus(NotificationStatus.SENT);
            else if (r.isSkipped()) n.setEmailStatus(NotificationStatus.SKIPPED);
            else {
                n.setEmailStatus(NotificationStatus.FAILED);
                n.setEmailError(r.getError());
            }
        } else {
            n.setEmailStatus(NotificationStatus.SKIPPED);
        }

        // ── SMS ────────────────────────────────────────────────────────────
        if (sendSms) {
            TwilioSmsService.SmsResult r = smsService.sendSms(
                    req.getRecipientPhone(), req.getBody());

            if (r.isSent())         n.setSmsStatus(NotificationStatus.SENT);
            else if (r.isSkipped()) n.setSmsStatus(NotificationStatus.SKIPPED);
            else {
                n.setSmsStatus(NotificationStatus.FAILED);
                n.setSmsError(r.getMessage());
            }
        } else {
            n.setSmsStatus(NotificationStatus.SKIPPED);
        }

        n.setSentAt(LocalDateTime.now());
        return toResponse(logRepository.save(n));
    }

    // ── Convenience helpers ───────────────────────────────────────────────

    public void sendBoth(String email, String phone, String name,
                         String subject, String body,
                         NotificationChannel channel, String refId) {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) return;
        send(SendNotificationRequest.builder()
                .recipientEmail(email).recipientPhone(phone)
                .recipientName(name != null ? name : "Member")
                .subject(subject).body(body)
                .type(NotificationType.BOTH)
                .channel(channel).referenceId(refId).build());
    }

    public void sendEmailOnly(String email, String name, String subject,
                              String body, NotificationChannel channel, String refId) {
        if (email == null || email.isBlank()) return;
        send(SendNotificationRequest.builder()
                .recipientEmail(email)
                .recipientName(name != null ? name : "Member")
                .subject(subject).body(body)
                .type(NotificationType.EMAIL)
                .channel(channel).referenceId(refId).build());
    }

    public void sendSmsOnly(String phone, String name, String body,
                            NotificationChannel channel, String refId) {
        if (phone == null || phone.isBlank()) return;
        send(SendNotificationRequest.builder()
                .recipientPhone(phone)
                .recipientName(name != null ? name : "Member")
                .body(body)
                .type(NotificationType.SMS)
                .channel(channel).referenceId(refId).build());
    }

    // ── Query ─────────────────────────────────────────────────────────────

    public Page<NotificationLogResponse> getAll(Pageable p) {
        return logRepository.findAll(p).map(this::toResponse);
    }

    public Page<NotificationLogResponse> getByMember(String mn, Pageable p) {
        return logRepository
                .findByRecipientMembershipNumberOrderByCreatedAtDesc(mn, p)
                .map(this::toResponse);
    }

    public Page<NotificationLogResponse> getByChannel(NotificationChannel ch, Pageable p) {
        return logRepository.findByChannelOrderByCreatedAtDesc(ch, p).map(this::toResponse);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("total",       logRepository.count());
        s.put("emailSent",   logRepository.countByEmailStatus(NotificationStatus.SENT));
        s.put("emailFailed", logRepository.countByEmailStatus(NotificationStatus.FAILED));
        s.put("smsSent",     logRepository.countBySmsStatus(NotificationStatus.SENT));
        s.put("smsFailed",   logRepository.countBySmsStatus(NotificationStatus.FAILED));
        return s;
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private NotificationLogResponse toResponse(NotificationLog n) {
        NotificationLogResponse r = new NotificationLogResponse();
        r.setId(n.getId());
        r.setRecipientMembershipNumber(n.getRecipientMembershipNumber());
        r.setRecipientName(n.getRecipientName());
        r.setRecipientEmail(n.getRecipientEmail());
        r.setRecipientPhone(n.getRecipientPhone());
        r.setType(n.getType());
        r.setChannel(n.getChannel());
        r.setSubject(n.getSubject());
        r.setBody(n.getBody());
        r.setEmailStatus(n.getEmailStatus());
        r.setSmsStatus(n.getSmsStatus());
        r.setEmailError(n.getEmailError());
        r.setSmsError(n.getSmsError());
        r.setReferenceId(n.getReferenceId());
        r.setSentAt(n.getSentAt());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
