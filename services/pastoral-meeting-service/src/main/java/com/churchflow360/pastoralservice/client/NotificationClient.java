package com.churchflow360.pastoralservice.client;

import com.churchflow360.pastoralservice.client.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name          = "notification-service",
        configuration = com.churchflow360.pastoralservice.config.FeignConfig.class,
        fallback      = NotificationClientFallback.class
)
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/send")
    ResponseEntity<Object> send(@RequestBody SendNotificationRequest request);
}
