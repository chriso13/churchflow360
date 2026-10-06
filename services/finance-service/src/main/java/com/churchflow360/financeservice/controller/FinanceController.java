package com.churchflow360.financeservice.controller;

import com.churchflow360.financeservice.dto.request.*;
import com.churchflow360.financeservice.dto.response.*;
import com.churchflow360.financeservice.model.DonationCategory;
import com.churchflow360.financeservice.security.GatewayAuthPrincipal;
import com.churchflow360.financeservice.service.FinanceService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Finance", description = "Tithes, Donations and Offerings")
public class FinanceController {

    private final FinanceService financeService;

    // ════════════════════════════════════════════════════════════════════════════
    // TITHES
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/tithes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Record a tithe (LEADER / PASTOR / ADMIN only)",
            description = "Member must exist in member-service."
    )
    public ResponseEntity<ApiResponse<TitheResponse>> recordTithe(
            @Valid @RequestBody RecordTitheRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tithe recorded", financeService.recordTithe(request)));
    }

    @GetMapping("/tithes/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get a tithe by ID (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<TitheResponse>> getTithe(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(financeService.getTithe(id)));
    }

    @GetMapping("/tithes/member/{membershipNumber}")
    @Operation(
            summary = "Get all tithes for a member",
            description = "MEMBER can only view their own tithes. LEADER and above can view any member."
    )
    public ResponseEntity<ApiResponse<PagedResponse<TitheResponse>>> getMemberTithes(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only view their own tithes
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own tithe records.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getMemberTithes(membershipNumber, pageable)));
    }

    @GetMapping("/tithes/member/{membershipNumber}/total/{year}")
    @Operation(
            summary = "Get total tithe amount for a member in a given year",
            description = "MEMBER can only view their own totals. LEADER and above can view any member."
    )
    public ResponseEntity<ApiResponse<BigDecimal>> getMemberTitheTotal(
            @PathVariable String membershipNumber,
            @PathVariable Integer year,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only view their own totals
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own tithe totals.");
        }

        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getMemberTitheTotal(membershipNumber, year)));
    }

    @GetMapping("/tithes")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get tithes within a date range (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<TitheResponse>>> getTithesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getTithesByDateRange(from, to, pageable)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DONATIONS
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/donations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Record a donation (LEADER / PASTOR / ADMIN only)",
            description = "membershipNumber is optional for anonymous/non-member donors."
    )
    public ResponseEntity<ApiResponse<DonationResponse>> recordDonation(
            @Valid @RequestBody RecordDonationRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Donation recorded", financeService.recordDonation(request)));
    }

    @GetMapping("/donations/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get a donation by ID (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<DonationResponse>> getDonation(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(financeService.getDonation(id)));
    }

    @GetMapping("/donations/member/{membershipNumber}")
    @Operation(
            summary = "Get all donations by a member",
            description = "MEMBER can only view their own donations. LEADER and above can view any member."
    )
    public ResponseEntity<ApiResponse<PagedResponse<DonationResponse>>> getMemberDonations(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // MEMBER can only view their own donations
        if ("MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own donation records.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getMemberDonations(membershipNumber, pageable)));
    }

    @GetMapping("/donations/category/{category}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get donations by category (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<DonationResponse>>> getDonationsByCategory(
            @Parameter(description = "BUILDING_FUND, MISSIONS, BENEVOLENCE, YOUTH_MINISTRY, " +
                    "CHILDREN_MINISTRY, MEDIA_MINISTRY, OUTREACH, " +
                    "SCHOLARSHIP, DISASTER_RELIEF, GENERAL")
            @PathVariable DonationCategory category,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getDonationsByCategory(category, pageable)));
    }

    @GetMapping("/donations")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get donations within a date range (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<DonationResponse>>> getDonationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getDonationsByDateRange(from, to, pageable)));
    }

    @PatchMapping("/donations/{id}/receipt")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Issue a tax receipt for a donation (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<DonationResponse>> issueReceipt(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Receipt issued", financeService.issueReceipt(id)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // OFFERINGS
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping("/offerings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Record a worship offering (LEADER / PASTOR / ADMIN only)",
            description = "Records the total collection for a service."
    )
    public ResponseEntity<ApiResponse<OfferingResponse>> recordOffering(
            @Valid @RequestBody RecordOfferingRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Offering recorded", financeService.recordOffering(request)));
    }

    @GetMapping("/offerings/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get an offering by ID (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<OfferingResponse>> getOffering(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(financeService.getOffering(id)));
    }

    @GetMapping("/offerings/date/{date}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get all offerings on a specific service date (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<List<OfferingResponse>>> getOfferingsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(financeService.getOfferingsByDate(date)));
    }

    @GetMapping("/offerings")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get offerings within a date range (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<OfferingResponse>>> getOfferingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                financeService.getOfferingsByDateRange(from, to, pageable)));
    }

    @PostMapping("/offerings/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Add an item to an existing offering (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<OfferingResponse>> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddOfferingItemRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added", financeService.addItemToOffering(id, request)));
    }

    @PatchMapping("/offerings/{id}/verify")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Mark an offering as counted and verified (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<OfferingResponse>> verifyOffering(
            @PathVariable UUID id,
            @RequestParam String verifiedBy,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Offering verified",
                financeService.verifyOffering(id, verifiedBy)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SUMMARY REPORT
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/finance/summary")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Get combined financial summary (PASTOR / ADMIN only)",
            description = "Returns total tithes + donations + offerings and grand total for a date range."
    )
    public ResponseEntity<ApiResponse<FinanceSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(financeService.getSummary(from, to)));
    }
}