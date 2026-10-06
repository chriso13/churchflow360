package com.churchflow360.financeservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Current fund balance snapshot.
 *
 * <p>Formula:
 * <pre>
 *   totalIncome    = all tithes + all donations + all offerings (ever)
 *   totalCommitted = all PAID expenses + all APPROVED (not yet paid) expenses
 *   availableBalance = totalIncome - totalCommitted
 * </pre>
 */
@Data
@Builder
public class FundBalanceResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalCommitted;
    private BigDecimal availableBalance;
}