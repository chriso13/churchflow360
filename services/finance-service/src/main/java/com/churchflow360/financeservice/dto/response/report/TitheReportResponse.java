package com.churchflow360.financeservice.dto.response.report;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitheReportResponse {

    private LocalDate from;
    private LocalDate to;
    private LocalDate generatedDate;
    private BigDecimal grandTotal;
    private int totalTithes;
    private long tithingMemberCount;
    private List<MonthlyTitheSummary> byMonth;
    private List<MemberTitheSummary> topTithers;
    private List<PaymentMethodSummary> byPaymentMethod;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyTitheSummary {
        private int year;
        private int month;
        private String monthName;
        private BigDecimal totalAmount;
        private int titheCount;
        private BigDecimal percentage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MemberTitheSummary {
        private String membershipNumber;
        private String memberName;
        private BigDecimal totalAmount;
        private int paymentCount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentMethodSummary {
        private String paymentMethod;
        private BigDecimal totalAmount;
        private int titheCount;
    }
}