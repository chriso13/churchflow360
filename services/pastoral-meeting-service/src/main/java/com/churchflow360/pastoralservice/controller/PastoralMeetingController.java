package com.churchflow360.pastoralservice.controller;

import com.churchflow360.pastoralservice.dto.request.*;
import com.churchflow360.pastoralservice.dto.response.*;
import com.churchflow360.pastoralservice.model.MeetingStatus;
import com.churchflow360.pastoralservice.service.PastoralMeetingService;
import com.churchflow360.pastoralservice.security.GatewayAuthPrincipal;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pastoral-meetings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pastoral Meetings", description = "Scheduling, status workflow and confidential notes")
public class PastoralMeetingController {

    private final PastoralMeetingService meetingService;

    // ════════════════════════════════════════════════════════════════════════════
    // REQUEST MEETING — any authenticated user
    // MEMBER can only request for themselves (memberMembershipNumber must match)
    // LEADER / PASTOR / ADMIN can request on behalf of any member
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Request a pastoral meeting",
            description = "MEMBER role can only request a meeting for themselves. " +
                    "LEADER and above can request on behalf of any member."
    )
    public ResponseEntity<ApiResponse<MeetingResponse>> request(
            @Valid @RequestBody CreateMeetingRequest req,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only request a meeting for themselves
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(req.getMemberMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only request a pastoral meeting for yourself.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Meeting requested", meetingService.requestMeeting(req)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GET BY ID — any authenticated; MEMBER sees own meetings only
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a meeting by ID",
            description = "MEMBER can only view their own meetings. LEADER and above can view any meeting."
    )
    public ResponseEntity<ApiResponse<MeetingResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        MeetingResponse meeting = meetingService.getMeeting(id);

        // MEMBER can only view meetings they are a party to
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(meeting.getMemberMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own pastoral meetings.");
        }

        return ResponseEntity.ok(ApiResponse.ok(meeting));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // STATUS WORKFLOW — PASTOR and ADMIN only
    // ════════════════════════════════════════════════════════════════════════════

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Confirm a meeting — REQUESTED → CONFIRMED (PASTOR / ADMIN only)",
            description = "Sets the final date, time and location."
    )
    public ResponseEntity<ApiResponse<MeetingResponse>> confirm(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmMeetingRequest req,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only confirm meetings assigned to them
        if ("PASTOR".equals(principal.getRole())) {
            MeetingResponse meeting = meetingService.getMeeting(id);
            if (!principal.isOwner(meeting.getPastorMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only confirm meetings assigned to you.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok("Meeting confirmed", meetingService.confirmMeeting(id, req)));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Reschedule a meeting — CONFIRMED → RESCHEDULED (PASTOR / ADMIN only)",
            description = "Moves the meeting to a new date/time. Reason is required."
    )
    public ResponseEntity<ApiResponse<MeetingResponse>> reschedule(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleMeetingRequest req,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only reschedule meetings assigned to them
        if ("PASTOR".equals(principal.getRole())) {
            MeetingResponse meeting = meetingService.getMeeting(id);
            if (!principal.isOwner(meeting.getPastorMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only reschedule meetings assigned to you.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok("Meeting rescheduled", meetingService.rescheduleMeeting(id, req)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a meeting — REQUESTED/CONFIRMED/RESCHEDULED → CANCELLED",
            description = "MEMBER can cancel their own meeting. PASTOR can cancel meetings assigned to them. " +
                    "ADMIN can cancel any meeting."
    )
    public ResponseEntity<ApiResponse<MeetingResponse>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelMeetingRequest req,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        MeetingResponse meeting = meetingService.getMeeting(id);

        // MEMBER — can only cancel their own meeting
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(meeting.getMemberMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only cancel your own pastoral meetings.");
        }

        // PASTOR — can only cancel meetings assigned to them
        if ("PASTOR".equals(principal.getRole())
                && !principal.isOwner(meeting.getPastorMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only cancel meetings assigned to you.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok("Meeting cancelled", meetingService.cancelMeeting(id, req)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Mark a meeting as completed — CONFIRMED → COMPLETED (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<MeetingResponse>> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only complete meetings assigned to them
        if ("PASTOR".equals(principal.getRole())) {
            MeetingResponse meeting = meetingService.getMeeting(id);
            if (!principal.isOwner(meeting.getPastorMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only complete meetings assigned to you.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok("Meeting marked as completed", meetingService.completeMeeting(id)));
    }

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Mark a meeting as no-show — CONFIRMED → NO_SHOW (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<MeetingResponse>> noShow(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only mark no-show on meetings assigned to them
        if ("PASTOR".equals(principal.getRole())) {
            MeetingResponse meeting = meetingService.getMeeting(id);
            if (!principal.isOwner(meeting.getPastorMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only mark no-show on meetings assigned to you.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok("Meeting marked as no-show", meetingService.markNoShow(id)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // QUERIES
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/pastor/{membershipNumber}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get all meetings for a pastor (PASTOR / ADMIN only)",
            description = "PASTOR can only view their own meetings. ADMIN can view any pastor's meetings."
    )
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> getByPastor(
            @PathVariable String membershipNumber,
            @Parameter(description = "Filter: REQUESTED, CONFIRMED, RESCHEDULED, COMPLETED, CANCELLED, NO_SHOW")
            @RequestParam(required = false) MeetingStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only view their own meetings
        if ("PASTOR".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own meeting schedule.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledDate").descending());
        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getMeetingsByPastor(membershipNumber, status, pageable)));
    }

    @GetMapping("/member/{membershipNumber}")
    @Operation(
            summary = "Get all meetings for a member",
            description = "MEMBER can only view their own meetings. LEADER and above can view any member's meetings."
    )
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> getByMember(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only view their own meeting history
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own pastoral meeting history.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledDate").descending());
        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getMeetingsByMember(membershipNumber, pageable)));
    }

    @GetMapping("/pastor/{membershipNumber}/schedule/{date}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get a pastor's schedule for a specific date (PASTOR / ADMIN only)",
            description = "PASTOR can only view their own schedule."
    )
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getPastorSchedule(
            @PathVariable String membershipNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        if ("PASTOR".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own schedule.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getPastorScheduleForDate(membershipNumber, date)));
    }

    @GetMapping("/pastor/{membershipNumber}/pending")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get pending (REQUESTED) meeting requests for a pastor (PASTOR / ADMIN only)",
            description = "PASTOR can only view their own pending requests."
    )
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getPendingRequests(
            @PathVariable String membershipNumber,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        if ("PASTOR".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own pending requests.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getPendingRequests(membershipNumber)));
    }

    @GetMapping("/pastor/{membershipNumber}/follow-up")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get meetings with overdue follow-ups for a pastor (PASTOR / ADMIN only)",
            description = "PASTOR can only view their own overdue follow-ups."
    )
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getOverdueFollowUps(
            @PathVariable String membershipNumber,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        if ("PASTOR".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own follow-up actions.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getOverdueFollowUps(membershipNumber)));
    }

    @GetMapping("/pastor/{membershipNumber}/stats")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get meeting statistics for a pastor (PASTOR / ADMIN only)",
            description = "PASTOR can only view their own statistics."
    )
    public ResponseEntity<ApiResponse<MeetingStatsResponse>> getStats(
            @PathVariable String membershipNumber,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        if ("PASTOR".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own meeting statistics.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getStatsForPastor(membershipNumber)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CONFIDENTIAL NOTES — PASTOR (assigned) and ADMIN only
    // The X-Requester-MembershipNumber header is now replaced by the
    // principal's membershipNumber from the JWT via X-Auth-Membership-Number.
    // The service layer still enforces that only the assigned pastor can access notes.
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Add a confidential note to a meeting (PASTOR / ADMIN only)",
            description = "Only the pastor assigned to this meeting can add notes."
    )
    public ResponseEntity<ApiResponse<ConfidentialNoteResponse>> addNote(
            @PathVariable UUID id,
            @Valid @RequestBody CreateNoteRequest req,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only add notes to meetings assigned to them
        if ("PASTOR".equals(principal.getRole())) {
            MeetingResponse meeting = meetingService.getMeeting(id);
            if (!principal.isOwner(meeting.getPastorMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only add notes to meetings assigned to you.");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Note added",
                        meetingService.addNote(id, req)));
    }

    @GetMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get confidential notes for a meeting (PASTOR / ADMIN only)",
            description = "Only the pastor assigned to this meeting can read notes."
    )
    public ResponseEntity<ApiResponse<List<ConfidentialNoteResponse>>> getNotes(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // PASTOR can only read notes from meetings assigned to them
        if ("PASTOR".equals(principal.getRole())) {
            MeetingResponse meeting = meetingService.getMeeting(id);
            if (!principal.isOwner(meeting.getPastorMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only view notes for meetings assigned to you.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok(meetingService.getNotes(id, principal.getMembershipNumber())));
    }

    @DeleteMapping("/notes/{noteId}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Delete a confidential note (author PASTOR only / ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        meetingService.deleteNote(noteId, principal.getMembershipNumber());
        return ResponseEntity.ok(ApiResponse.ok("Note deleted", null));
    }
}