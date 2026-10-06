package com.churchflow360.financeservice.controller;

import com.churchflow360.financeservice.dto.request.*;
import com.churchflow360.financeservice.dto.response.*;
import com.churchflow360.financeservice.model.ExpenseCategory;
import com.churchflow360.financeservice.model.ExpenseStatus;
import com.churchflow360.financeservice.security.GatewayAuthPrincipal;
import com.churchflow360.financeservice.service.ExpenseService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Expenses", description = "Church expense tracking and approval workflow")
public class ExpenseController {

    private final ExpenseService expenseService;

    // ════════════════════════════════════════════════════════════════════════════
    // CRUD
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "Create a new expense — DRAFT status (LEADER / PASTOR / ADMIN only)",
            description = "Call PATCH /{id}/submit to send it for approval."
    )
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Expense created", expenseService.createExpense(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get an expense by ID (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getExpense(id)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // QUERIES
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get expenses by status (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getByStatus(
            @Parameter(description = "DRAFT, SUBMITTED, APPROVED, PAID, REJECTED, CANCELLED")
            @PathVariable ExpenseStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expenseDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getByStatus(status, pageable)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get expenses by category (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getByCategory(
            @PathVariable ExpenseCategory category,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expenseDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getByCategory(category, pageable)));
    }

    @GetMapping("/submitter/{membershipNumber}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get all expenses submitted by a member (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getBySubmitter(
            @PathVariable String membershipNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.getBySubmitter(membershipNumber, pageable)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get expenses within a date range (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expenseDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getByDateRange(from, to, pageable)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get all expenses awaiting approval (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getPendingApproval(
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getPendingApproval()));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get overdue expenses (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getOverdue(
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getOverdue()));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get expense summary and counts for a date range (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ExpenseSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getSummary(from, to)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // APPROVAL WORKFLOW
    // ════════════════════════════════════════════════════════════════════════════

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Submit expense for approval — DRAFT → SUBMITTED (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ExpenseResponse>> submit(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Expense submitted for approval", expenseService.submit(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Approve an expense — SUBMITTED → APPROVED (PASTOR / ADMIN only)",
            description = "Approver must be a different member from the submitter (separation of duties)."
    )
    public ResponseEntity<ApiResponse<ExpenseResponse>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveExpenseRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Expense approved", expenseService.approve(id, request)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(
            summary = "Reject an expense — SUBMITTED → REJECTED (PASTOR / ADMIN only)",
            description = "Rejection reason is required."
    )
    public ResponseEntity<ApiResponse<ExpenseResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectExpenseRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Expense rejected", expenseService.reject(id, request)));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Mark an expense as paid — APPROVED → PAID (ADMIN only)",
            description = "Records the payment method and reference."
    )
    public ResponseEntity<ApiResponse<ExpenseResponse>> markPaid(
            @PathVariable UUID id,
            @Valid @RequestBody MarkPaidRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Expense marked as paid", expenseService.markPaid(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Cancel an expense (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<ExpenseResponse>> cancel(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Expense cancelled", expenseService.cancel(id, reason)));
    }
}