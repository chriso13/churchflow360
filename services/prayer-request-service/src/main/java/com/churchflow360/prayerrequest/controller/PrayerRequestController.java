package com.churchflow360.prayerrequest.controller;

import com.churchflow360.prayerrequest.dto.request.*;
import com.churchflow360.prayerrequest.dto.response.*;
import com.churchflow360.prayerrequest.model.*;
import com.churchflow360.prayerrequest.service.PrayerRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController @RequestMapping("/api/v1/prayer-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Prayer Requests", description = "Church prayer request management")
public class PrayerRequestController {

    private final PrayerRequestService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a new prayer request")
    public ResponseEntity<ApiResponse<PrayerRequestResponse>> create(
            @Valid @RequestBody CreatePrayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Prayer request submitted", service.create(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all visible prayer requests")
    public ResponseEntity<ApiResponse<Page<PrayerRequestResponse>>> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size);
        String role = getRole(auth);
        Page<PrayerRequestResponse> result = Boolean.TRUE.equals(activeOnly)
                ? service.getActive(role, pageable)
                : service.getAll(role, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PrayerRequestResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getOne(id)));
    }

    @GetMapping("/member/{membershipNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get prayer requests by member")
    public ResponseEntity<ApiResponse<Page<PrayerRequestResponse>>> getByMember(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.getByMember(membershipNumber, PageRequest.of(page, size))));
    }

    @GetMapping("/urgent")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN', 'LEADER')")
    @Operation(summary = "Get all urgent active prayer requests")
    public ResponseEntity<ApiResponse<List<PrayerRequestResponse>>> getUrgent() {
        return ResponseEntity.ok(ApiResponse.ok(service.getUrgent()));
    }

    @PostMapping("/{id}/pray")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Increment prayer count — 'I prayed for this'")
    public ResponseEntity<ApiResponse<PrayerRequestResponse>> pray(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.incrementPrayerCount(id)));
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark request as answered with testimony")
    public ResponseEntity<ApiResponse<PrayerRequestResponse>> markAnswered(
            @PathVariable UUID id, @Valid @RequestBody AnswerPrayerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Praise God! Marked as answered",
                service.markAnswered(id, request)));
    }

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Update request status")
    public ResponseEntity<ApiResponse<PrayerRequestResponse>> updateStatus(
            @PathVariable UUID id, @PathVariable RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", service.updateStatus(id, status)));
    }

    @PatchMapping("/{id}/assign/{pastorNumber}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Assign a pastor to a prayer request")
    public ResponseEntity<ApiResponse<PrayerRequestResponse>> assign(
            @PathVariable UUID id, @PathVariable String pastorNumber) {
        return ResponseEntity.ok(ApiResponse.ok("Pastor assigned", service.assignPastor(id, pastorNumber)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(service.getStats()));
    }

    private String getRole(Authentication auth) {
        return auth.getAuthorities().stream().findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "")).orElse("MEMBER");
    }
}
