package com.churchflow360.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Circuit breaker fallback endpoints.
 * Uses @RequestMapping (no method restriction) so POST, PUT, DELETE etc.
 * all get the same 503 fallback response instead of 405 Method Not Allowed.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return fallback("auth-service", "Authentication service is temporarily unavailable.");
    }

    @RequestMapping("/member")
    public ResponseEntity<Map<String, Object>> memberFallback() {
        return fallback("member-service", "Member service is temporarily unavailable.");
    }

    @RequestMapping("/attendance")
    public ResponseEntity<Map<String, Object>> attendanceFallback() {
        return fallback("attendance-service", "Attendance service is temporarily unavailable.");
    }

    @RequestMapping("/event")
    public ResponseEntity<Map<String, Object>> eventFallback() {
        return fallback("event-service", "Event service is temporarily unavailable.");
    }

    @RequestMapping("/pastoral")
    public ResponseEntity<Map<String, Object>> pastoralFallback() {
        return fallback("pastoral-meeting-service", "Pastoral meeting service is temporarily unavailable.");
    }

    @RequestMapping("/finance")
    public ResponseEntity<Map<String, Object>> financeFallback() {
        return fallback("finance-service", "Finance service is temporarily unavailable.");
    }

    @RequestMapping("/notification")
    public ResponseEntity<Map<String, Object>> notificationFallback() {
        return fallback("notification-service", "Notification service is temporarily unavailable.");
    }

    @RequestMapping("/small-groups")
    public ResponseEntity<Map<String, Object>> smallGroupsFallback() {
        return fallback("small-groups-service", "Small groups service is temporarily unavailable.");
    }

    @RequestMapping("/prayer")
    public ResponseEntity<Map<String, Object>> prayerFallback() {
        return fallback("prayer-request-service", "Prayer request service is temporarily unavailable.");
    }

    @RequestMapping("/announcement")
    public ResponseEntity<Map<String, Object>> announcementFallback() {
        return fallback("announcement-service", "Announcement service is temporarily unavailable.");
    }

    private ResponseEntity<Map<String, Object>> fallback(String service, String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success",   false,
                "message",   message + " Please try again later.",
                "service",   service,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}