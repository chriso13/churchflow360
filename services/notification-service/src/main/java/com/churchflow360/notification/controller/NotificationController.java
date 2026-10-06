package com.churchflow360.notification.controller;

import com.churchflow360.notification.dto.request.SendNotificationRequest;
import com.churchflow360.notification.dto.response.*;
import com.churchflow360.notification.model.NotificationChannel;
import com.churchflow360.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Send and manage church notifications via Brevo Email and MySMSGate SMS")
public class NotificationController {

    private final NotificationService service;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Send a manual notification (email, SMS, or both)")
    public ResponseEntity<ApiResponse<NotificationLogResponse>> send(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Notification sent", service.send(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get all notification logs (PASTOR, ADMIN)")
    public ResponseEntity<ApiResponse<Page<NotificationLogResponse>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(service.getAll(pageable)));
    }

    @GetMapping("/member/{membershipNumber}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get notifications sent to a specific member")
    public ResponseEntity<ApiResponse<Page<NotificationLogResponse>>> getByMember(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.getByMember(membershipNumber, PageRequest.of(page, size))));
    }

    @GetMapping("/channel/{channel}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get notifications by channel/event type")
    public ResponseEntity<ApiResponse<Page<NotificationLogResponse>>> getByChannel(
            @PathVariable NotificationChannel channel,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.getByChannel(channel, PageRequest.of(page, size))));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(service.getStats()));
    }
}
