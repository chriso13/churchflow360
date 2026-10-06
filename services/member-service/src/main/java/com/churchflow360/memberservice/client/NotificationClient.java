package com.churchflow360.memberservice.client;

import com.churchflow360.memberservice.client.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for notification-service.
 *
 * The gateway injects X-Auth-* headers so notification-service accepts the call.
 * We call the service DIRECTLY (lb://notification-service) to avoid a gateway
 * round-trip and to bypass the need for a JWT in service-to-service calls.
 *
 * FeignConfig injects the required X-Auth-* headers from the current request context.
 */
@FeignClient(
        name            = "notification-service",
        configuration   = com.churchflow360.memberservice.config.FeignConfig.class,
        fallback        = NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/send")
    ResponseEntity<Object> send(@RequestBody SendNotificationRequest request);
}
