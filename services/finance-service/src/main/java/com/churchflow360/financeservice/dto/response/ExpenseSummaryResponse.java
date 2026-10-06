package com.churchflow360.financeservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseSummaryResponse {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;     // SUBMITTED + APPROVED
    private BigDecimal totalDraft;
    private long countPaid;
    private long countPending;
    private long countDraft;
    private long countOverdue;
    private long countRejected;
}