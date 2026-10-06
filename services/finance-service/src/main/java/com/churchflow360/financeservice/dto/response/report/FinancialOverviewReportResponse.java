package com.churchflow360.financeservice.dto.response.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * High-level financial overview — income vs expenses dashboard.
 * Combines tithes, donations, offerings and expenses in one report.
 */
@Data
@Builder
public class FinancialOverviewReportResponse {

    // ── Period ─────────────────────────────────────────────────────────────────
    private LocalDate from;
    private LocalDate to;
    private LocalDate generatedDate;

    // ── Income ─────────────────────────────────────────────────────────────────
    private BigDecimal totalTithes;
    private BigDecimal totalDonations;
    private BigDecimal totalOfferings;
    private BigDecimal totalIncome;

    // ── Expenses ───────────────────────────────────────────────────────────────
    private BigDecimal totalExpensesPaid;
    private BigDecimal totalExpensesApproved;   // committed but not yet paid
    private BigDecimal totalExpensesPending;    // submitted, awaiting approval
    private BigDecimal availableBalance;

    // ── Net ────────────────────────────────────────────────────────────────────
    private BigDecimal netPosition;             // totalIncome - totalExpensesPaid

    // ── Counts ─────────────────────────────────────────────────────────────────
    private Long titheCount;
    private Long donationCount;
    private Long offeringCount;
    private Long expenseCount;
    private Long tithingMemberCount;

    // ── Monthly trend (income vs expenses per month) ───────────────────────────
    private List<MonthlyTrendItem> monthlyTrend;

    // ── Expense breakdown by category ──────────────────────────────────────────
    private List<ExpenseCategorySummary> expensesByCategory;

    // ── Inner classes ──────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class MonthlyTrendItem {
        private Integer    year;
        private Integer    month;
        private String     monthName;
        private BigDecimal tithes;
        private BigDecimal donations;
        private BigDecimal offerings;
        private BigDecimal totalIncome;
        private BigDecimal expensesPaid;
        private BigDecimal netPosition;
    }

    @Data
    @Builder
    public static class ExpenseCategorySummary {
        private String     category;
        private BigDecimal totalAmount;
        private Integer    count;
        private BigDecimal percentage;
    }
}