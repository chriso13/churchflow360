package com.churchflow360.announcement.controller;

import com.churchflow360.announcement.dto.request.CreateAnnouncementRequest;
import com.churchflow360.announcement.dto.response.*;
import com.churchflow360.announcement.model.*;
import com.churchflow360.announcement.service.AnnouncementService;
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

@RestController @RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Announcements", description = "Church announcement management")
public class AnnouncementController {

    private final AnnouncementService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Create an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Announcement created", service.create(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active announcements visible to the caller")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getActive(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String role = getRole(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                service.getActive(role, PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get all announcements including drafts (PASTOR, ADMIN)")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AnnouncementStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AnnouncementResponse> result = status != null
                ? service.getByStatus(status, pageable)
                : service.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getOne(id)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getByCategory(
            @PathVariable AnnouncementCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.getByCategory(category, PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Publish a draft announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Published", service.publish(id)));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Archive an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Archived", service.archive(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Update an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", service.update(id, request)));
    }

    @PostMapping("/{id}/view")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Increment view count")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> view(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.incrementViews(id)));
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
