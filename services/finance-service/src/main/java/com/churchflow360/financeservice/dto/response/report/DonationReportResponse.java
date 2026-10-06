package com.churchflow360.financeservice.dto.response.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Donation report covering a date range.
 * Breaks down by category, donor type, and payment method.
 */
@Data
@Builder
public class DonationReportResponse {

    // ── Period ─────────────────────────────────────────────────────────────────
    private LocalDate from;
    private LocalDate to;
    private LocalDate generatedDate;

    // ── Totals ─────────────────────────────────────────────────────────────────
    private BigDecimal grandTotal;
    private Integer    totalDonations;
    private Integer    anonymousDonations;
    private Integer    memberDonations;
    private Integer    pendingReceipts;        // issued = false

    // ── Breakdown by category ──────────────────────────────────────────────────
    private List<CategorySummary> byCategory;

    // ── Breakdown by payment method ────────────────────────────────────────────
    private List<PaymentMethodSummary> byPaymentMethod;

    // ── Top donors (non-anonymous) ─────────────────────────────────────────────
    private List<DonorSummary> topDonors;

    // ── Inner classes ──────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class CategorySummary {
        private String     category;
        private String     fundName;
        private BigDecimal totalAmount;
        private Integer    donationCount;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    public static class PaymentMethodSummary {
        private String     paymentMethod;
        private BigDecimal totalAmount;
        private Integer    donationCount;
    }

    @Data
    @Builder
    public static class DonorSummary {
        private String     membershipNumber;
        private String     donorName;
        private BigDecimal totalDonated;
        private Integer    donationCount;
    }
}