package com.churchflow360.financeservice.dto.response.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Worship offering report — covers a date range.
 * Breaks down by type, service, and payment method.
 */
@Data
@Builder
public class OfferingReportResponse {

    // ── Period ─────────────────────────────────────────────────────────────────
    private LocalDate from;
    private LocalDate to;
    private LocalDate generatedDate;

    // ── Totals ─────────────────────────────────────────────────────────────────
    private BigDecimal grandTotal;
    private BigDecimal totalCash;
    private BigDecimal totalCheck;
    private BigDecimal totalDigital;
    private Integer    totalServices;
    private Integer    totalGivers;
    private BigDecimal averagePerService;

    // ── Breakdown by offering type ─────────────────────────────────────────────
    private List<OfferingTypeSummary> byType;

    // ── Breakdown by month ─────────────────────────────────────────────────────
    private List<MonthlyOfferingSummary> byMonth;

    // ── Individual service records ─────────────────────────────────────────────
    private List<OfferingServiceRecord> services;

    // ── Inner classes ──────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class OfferingTypeSummary {
        private String     offeringType;
        private BigDecimal totalAmount;
        private Integer    serviceCount;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    public static class MonthlyOfferingSummary {
        private Integer    year;
        private Integer    month;
        private String     monthName;
        private BigDecimal totalAmount;
        private Integer    serviceCount;
        private BigDecimal cashAmount;
        private BigDecimal checkAmount;
        private BigDecimal digitalAmount;
    }

    @Data
    @Builder
    public static class OfferingServiceRecord {
        private LocalDate  serviceDate;
        private String     serviceDescription;
        private String     offeringType;
        private BigDecimal totalAmount;
        private BigDecimal cashAmount;
        private BigDecimal checkAmount;
        private BigDecimal digitalAmount;
        private Integer    giverCount;
        private String     countedBy;
        private String     verifiedBy;
        private Boolean    verified;
    }
}