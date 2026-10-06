package com.churchflow360.financeservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinanceSummaryResponse {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalTithes;
    private BigDecimal totalDonations;
    private BigDecimal totalOfferings;
    private BigDecimal grandTotal;
    private long titheCount;
    private long donationCount;
    private long offeringCount;
    private long tithingMemberCount;
}