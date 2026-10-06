package com.churchflow360.financeservice.controller;

import com.churchflow360.financeservice.dto.response.ApiResponse;
import com.churchflow360.financeservice.dto.response.report.*;
import com.churchflow360.financeservice.security.GatewayAuthPrincipal;
import com.churchflow360.financeservice.service.report.FinanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Finance Reports",
        description = "Year-end tithe statements, offering reports, donation reports and financial overview " +
                "(PASTOR / ADMIN only)")
public class FinanceReportController {

    private final FinanceReportService reportService;

    // ════════════════════════════════════════════════════════════════════════════
    // YEAR-END TITHE STATEMENT
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/tithes/year-end/{membershipNumber}/{year}")
    @Operation(
            summary = "Generate year-end tithe statement for a member (PASTOR / ADMIN only)",
            description = "Returns a complete tithe statement for the given year with monthly breakdown. " +
                    "Returns 404 if no tithes found for this member in the given year."
    )
    public ResponseEntity<ApiResponse<YearEndTitheStatementResponse>> yearEndTitheStatement(
            @Parameter(description = "Membership number e.g. MEM-0609-2026-113045")
            @PathVariable String membershipNumber,
            @Parameter(description = "Year e.g. 2026")
            @PathVariable Integer year,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateYearEndTitheStatement(membershipNumber, year)));
    }

    @GetMapping("/tithes")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Church-wide tithe report (LEADER, PASTOR, ADMIN only)")
    public ResponseEntity<ApiResponse<TitheReportResponse>> getTitheReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.generateTitheReport(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // OFFERING REPORT
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/offerings")
    @Operation(summary = "Generate worship offering report for a date range (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<OfferingReportResponse>> offeringReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateOfferingReport(from, to)));
    }

    @GetMapping("/offerings/year/{year}")
    @Operation(summary = "Generate full-year worship offering report (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<OfferingReportResponse>> offeringReportByYear(
            @PathVariable Integer year,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateOfferingReport(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DONATION REPORT
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/donations")
    @Operation(summary = "Generate donation report for a date range (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<DonationReportResponse>> donationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateDonationReport(from, to)));
    }

    @GetMapping("/donations/year/{year}")
    @Operation(summary = "Generate full-year donation report (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<DonationReportResponse>> donationReportByYear(
            @PathVariable Integer year,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateDonationReport(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EXPENSE REPORT
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/expenses")
    @Operation(summary = "Generate expense report for a date range (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ExpenseReportResponse>> expenseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateExpenseReport(from, to)));
    }

    @GetMapping("/expenses/year/{year}")
    @Operation(summary = "Generate full-year expense report (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ExpenseReportResponse>> expenseReportByYear(
            @PathVariable Integer year,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateExpenseReport(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // FINANCIAL OVERVIEW
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/overview")
    @Operation(summary = "Generate financial overview dashboard for a date range (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<FinancialOverviewReportResponse>> overviewReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateOverviewReport(from, to)));
    }

    @GetMapping("/overview/year/{year}")
    @Operation(summary = "Generate full-year financial overview (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<FinancialOverviewReportResponse>> overviewReportByYear(
            @PathVariable Integer year,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateOverviewReport(from, to)));
    }

    @GetMapping("/overview/current-year")
    @Operation(summary = "Generate financial overview for the current calendar year (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<FinancialOverviewReportResponse>> overviewCurrentYear(
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        int year = LocalDate.now().getYear();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateOverviewReport(from, to)));
    }
}