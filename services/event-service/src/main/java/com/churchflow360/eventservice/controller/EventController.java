package com.churchflow360.eventservice.controller;

import com.churchflow360.eventservice.dto.request.CreateEventRequest;
import com.churchflow360.eventservice.dto.request.RsvpRequest;
import com.churchflow360.eventservice.dto.request.UpdateEventRequest;
import com.churchflow360.eventservice.dto.response.*;
import com.churchflow360.eventservice.model.EventType;
import com.churchflow360.eventservice.security.GatewayAuthPrincipal;
import com.churchflow360.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Events", description = "Church event scheduling and RSVP management")
public class EventController {

    private final EventService eventService;

    // ════════════════════════════════════════════════════════════════════════════
    // EVENT CRUD
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Create a new event — DRAFT status (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<EventResponse>> create(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Event created", eventService.createEvent(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an event by ID")
    public ResponseEntity<ApiResponse<EventResponse>> getById(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Update an event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<EventResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Event updated", eventService.updateEvent(id, request)));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Publish an event — DRAFT → PUBLISHED (LEADER / PASTOR / ADMIN only)",
            description = "Publishes an event.created Kafka event which triggers notifications to all members."
    )
    public ResponseEntity<ApiResponse<EventResponse>> publish(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Event published", eventService.publishEvent(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Cancel an event (PASTOR / ADMIN only)",
            description = "Publishes an event.cancelled Kafka event which notifies all members who RSVPd."
    )
    public ResponseEntity<ApiResponse<EventResponse>> cancel(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Event cancelled", eventService.cancelEvent(id)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Mark an event as completed (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<EventResponse>> complete(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Event marked as completed", eventService.completeEvent(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an event and all its RSVPs (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.ok("Event deleted", null));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EVENT QUERIES — any authenticated user
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping
    @Operation(summary = "List all published events (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<EventResponse>>> getPublished(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        return ResponseEntity.ok(ApiResponse.ok(eventService.getPublishedEvents(pageable)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming published events from today")
    public ResponseEntity<ApiResponse<PagedResponse<EventResponse>>> getUpcoming(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        return ResponseEntity.ok(ApiResponse.ok(eventService.getUpcomingEvents(pageable)));
    }

    @GetMapping("/type/{eventType}")
    @Operation(summary = "List published events by type")
    public ResponseEntity<ApiResponse<PagedResponse<EventResponse>>> getByType(
            @Parameter(description = "SUNDAY_SERVICE, WEDNESDAY_SERVICE, BIBLE_STUDY, " +
                    "PRAYER_MEETING, YOUTH_SERVICE, CONFERENCE, RETREAT, " +
                    "SMALL_GROUP, BAPTISM, SPECIAL_EVENT")
            @PathVariable EventType eventType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventsByType(eventType, pageable)));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "List published events on a specific date")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventsByDate(date)));
    }

    @GetMapping("/range")
    @Operation(summary = "List published events within a date range")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventsByDateRange(from, to)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search events by keyword (matches title, description, tags)")
    public ResponseEntity<ApiResponse<PagedResponse<EventResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(eventService.searchEvents(keyword, pageable)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // RSVP
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/{id}/rsvp")
    @Operation(
            summary = "Submit or update an RSVP",
            description = "MEMBER role can only RSVP for themselves. " +
                    "LEADER and above can RSVP on behalf of any member. " +
                    "Status values: ATTENDING, NOT_ATTENDING, MAYBE"
    )
    public ResponseEntity<ApiResponse<RsvpResponse>> submitRsvp(
            @PathVariable String id,
            @Valid @RequestBody RsvpRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only RSVP for themselves
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(request.getMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only submit an RSVP for yourself.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("RSVP submitted", eventService.submitRsvp(id, request)));
    }

    @GetMapping("/{id}/rsvps")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "List all RSVPs for an event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<List<RsvpResponse>>> getRsvps(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventRsvps(id)));
    }

    @GetMapping("/{id}/rsvps/attending")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "List members confirmed ATTENDING for an event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<List<RsvpResponse>>> getAttending(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getAttendingRsvps(id)));
    }

    @GetMapping("/{id}/rsvps/{membershipNumber}")
    @Operation(
            summary = "Get a specific member's RSVP for an event",
            description = "MEMBER role can only view their own RSVP. LEADER and above can view any member."
    )
    public ResponseEntity<ApiResponse<RsvpResponse>> getMemberRsvp(
            @PathVariable String id,
            @Parameter(description = "Membership number e.g. MEM-0609-2026-113045")
            @PathVariable String membershipNumber,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only view their own RSVP
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own RSVP.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(eventService.getMemberRsvp(id, membershipNumber)));
    }

    @GetMapping("/rsvps/member/{membershipNumber}")
    @Operation(
            summary = "Get all RSVPs submitted by a member",
            description = "MEMBER role can only view their own RSVP history. LEADER and above can view any member."
    )
    public ResponseEntity<ApiResponse<PagedResponse<RsvpResponse>>> getMemberRsvpHistory(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only view their own RSVP history
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own RSVP history.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.ok(eventService.getMemberRsvpHistory(membershipNumber, pageable)));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get RSVP statistics for an event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<EventStatsResponse>> getStats(
            @PathVariable String id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventStats(id)));
    }
}