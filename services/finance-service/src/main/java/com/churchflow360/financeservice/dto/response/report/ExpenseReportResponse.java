package com.churchflow360.financeservice.dto.response.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Expense report covering a date range.
 * Breaks down by category, status, and vendor.
 */
@Data
@Builder
public class ExpenseReportResponse {

    // ── Period ─────────────────────────────────────────────────────────────────
    private LocalDate from;
    private LocalDate to;
    private LocalDate generatedDate;

    // ── Totals by status ───────────────────────────────────────────────────────
    private BigDecimal totalPaid;
    private BigDecimal totalApproved;
    private BigDecimal totalPending;
    private BigDecimal totalRejected;
    private BigDecimal totalCancelled;
    private BigDecimal grandTotal;

    // ── Counts ─────────────────────────────────────────────────────────────────
    private Integer countPaid;
    private Integer countApproved;
    private Integer countPending;
    private Integer countOverdue;

    // ── Breakdown by category ──────────────────────────────────────────────────
    private List<CategoryBreakdown> byCategory;

    // ── Top vendors ────────────────────────────────────────────────────────────
    private List<VendorSummary> topVendors;

    // ── Overdue expenses ───────────────────────────────────────────────────────
    private List<OverdueExpenseItem> overdueExpenses;

    // ── Inner classes ──────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class CategoryBreakdown {
        private String     category;
        private BigDecimal totalPaid;
        private Integer    count;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    public static class VendorSummary {
        private String     vendorName;
        private BigDecimal totalAmount;
        private Integer    expenseCount;
    }

    @Data
    @Builder
    public static class OverdueExpenseItem {
        private String     expenseId;
        private String     title;
        private BigDecimal amount;
        private LocalDate  dueDate;
        private String     status;
        private Long       daysOverdue;
    }
}