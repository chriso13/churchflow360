package com.churchflow360.attendanceservice.controller;

import com.churchflow360.attendanceservice.dto.request.CheckInRequest;
import com.churchflow360.attendanceservice.dto.request.CreateServiceEventRequest;
import com.churchflow360.attendanceservice.dto.request.QrCheckInRequest;
import com.churchflow360.attendanceservice.dto.response.*;
import com.churchflow360.attendanceservice.security.GatewayAuthPrincipal;
import com.churchflow360.attendanceservice.service.AttendanceReportService;
import com.churchflow360.attendanceservice.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Attendance", description = "Church service check-ins, QR codes and reports")
public class AttendanceController {

    private final AttendanceService       attendanceService;
    private final AttendanceReportService reportService;

    // ════════════════════════════════════════════════════════════════════════════
    // SERVICE EVENTS — create: LEADER+; view: any authenticated
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Create a new church service or event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ServiceEventResponse>> createEvent(
            @Valid @RequestBody CreateServiceEventRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Service event created",
                        attendanceService.createServiceEvent(request)));
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Get a service event by ID")
    public ResponseEntity<ApiResponse<ServiceEventResponse>> getEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getServiceEvent(id)));
    }

    @GetMapping("/events/date/{date}")
    @Operation(summary = "Get all service events on a specific date")
    public ResponseEntity<ApiResponse<List<ServiceEventResponse>>> getEventsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getServicesByDate(date)));
    }

    @GetMapping("/events")
    @Operation(summary = "Get service events within a date range")
    public ResponseEntity<ApiResponse<List<ServiceEventResponse>>> getEventsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok(attendanceService.getServicesByDateRange(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // QR CODE — generate: LEADER+; download image: any authenticated
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/events/{id}/qr")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Generate a QR code for a service event (LEADER / PASTOR / ADMIN only)",
            description = "Returns a Base64 PNG and token valid for 30 minutes."
    )
    public ResponseEntity<ApiResponse<QrCodeResponse>> generateQr(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("QR code generated", attendanceService.generateQrCode(id)));
    }

    @GetMapping(value = "/events/{id}/qr/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Download the current QR code as a PNG image")
    public ResponseEntity<byte[]> getQrImage(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        byte[] image = attendanceService.getQrCodeImage(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-checkin.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CHECK-IN
    // Manual check-in: LEADER+ only (leader checks in a member)
    // QR check-in: any authenticated (member scans own QR)
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/checkin")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Manual check-in (LEADER / PASTOR / ADMIN only)",
            description = "Leader checks in a member by entering their membership number."
    )
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> checkInManual(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Check-in successful",
                        attendanceService.checkInManual(request)));
    }

    @PostMapping("/checkin/qr")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "QR code self check-in",
            description = "Member scans the QR code. " +
                    "MEMBER role can only check in themselves — cannot check in other members. " +
                    "LEADER and above can check in any member."
    )
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> checkInByQr(
            @Valid @RequestBody QrCheckInRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only check in themselves
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(request.getMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only check in yourself.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("QR check-in successful",
                        attendanceService.checkInByQr(request)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ATTENDANCE QUERIES
    // Attendees list: LEADER+ only
    // Member history/count: MEMBER sees own only; LEADER+ sees all
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/events/{id}/attendees")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "List all attendees for a service event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getAttendees(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok(attendanceService.getAttendanceByEvent(id)));
    }

    @GetMapping("/members/{membershipNumber}/history")
    @Operation(
            summary = "Get attendance history for a member",
            description = "MEMBER role can only view their own history. LEADER and above can view any member."
    )
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceRecordResponse>>> getMemberHistory(
            @Parameter(description = "Membership number e.g. MEM-0609-2026-113045")
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER role — can only view their own history
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own attendance history.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.ok(attendanceService.getMemberHistory(membershipNumber, pageable)));
    }

    @GetMapping("/members/{membershipNumber}/count")
    @Operation(
            summary = "Count services attended by a member in a date range",
            description = "MEMBER role can only count their own attendance. LEADER and above can query any member."
    )
    public ResponseEntity<ApiResponse<Long>> getMemberAttendanceCount(
            @Parameter(description = "Membership number e.g. MEM-0609-2026-113045")
            @PathVariable String membershipNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER role — can only count their own attendance
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own attendance count.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(attendanceService.getMemberAttendanceCount(
                        membershipNumber, from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // STATISTICS — LEADER and above only
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get attendance statistics for a date range (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<AttendanceStatsResponse>> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getStats(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PDF REPORTS — LEADER and above only
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping(value = "/reports/event/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Download PDF report for a single service event (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<byte[]> downloadServiceReport(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        byte[] pdf = reportService.generateServiceReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/reports/range", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Download PDF summary report for a date range (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<byte[]> downloadRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        byte[] pdf = reportService.generateRangeReport(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance-summary-" + from + "-to-" + to + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}