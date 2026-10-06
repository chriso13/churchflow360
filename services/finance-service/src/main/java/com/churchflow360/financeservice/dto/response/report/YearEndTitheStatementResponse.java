package com.churchflow360.financeservice.dto.response.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Year-end tithe statement for a single member.
 * Suitable for tax deduction purposes.
 */
@Data
@Builder
public class YearEndTitheStatementResponse {

    // ── Header ─────────────────────────────────────────────────────────────────
    private String  membershipNumber;
    private String  memberName;
    private Integer year;
    private LocalDate generatedDate;

    // ── Summary ────────────────────────────────────────────────────────────────
    private BigDecimal totalTithed;
    private Integer    numberOfPayments;
    private String     currency;

    // ── Monthly breakdown ──────────────────────────────────────────────────────
    private List<MonthlyTitheSummary> monthlyBreakdown;

    // ── Individual records ─────────────────────────────────────────────────────
    private List<TitheLineItem> titheRecords;

    // ── Church info (for the statement header) ─────────────────────────────────
    private String churchName;
    private String churchAddress;
    private String churchPhone;
    private String churchEmail;
    private String taxExemptNumber;

    // ── Inner classes ──────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class MonthlyTitheSummary {
        private String     month;         // e.g. "JANUARY"
        private Integer    year;
        private BigDecimal totalAmount;
        private Integer    paymentCount;
    }

    @Data
    @Builder
    public static class TitheLineItem {
        private LocalDate  paymentDate;
        private String     titheMonth;
        private Integer    titheYear;
        private BigDecimal amount;
        private String     paymentMethod;
        private String     referenceNumber;
        private String     notes;
    }
}