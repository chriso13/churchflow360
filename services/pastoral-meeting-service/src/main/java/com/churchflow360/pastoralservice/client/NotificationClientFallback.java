package com.churchflow360.pastoralservice.client;

import com.churchflow360.pastoralservice.client.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback when notification-service is unavailable.
 * Pastoral meeting operations still succeed — notifications are best-effort.
 */
@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public ResponseEntity<Object> send(SendNotificationRequest request) {
        log.warn("notification-service unavailable — skipping notification to {} channel={}",
                request.getRecipientName(), request.getChannel());
        return ResponseEntity.ok(null);
    }
}
