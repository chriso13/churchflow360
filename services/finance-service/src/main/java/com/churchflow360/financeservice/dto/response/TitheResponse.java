package com.churchflow360.financeservice.dto.response;

import com.churchflow360.financeservice.model.PaymentMethod;
import com.churchflow360.financeservice.model.TithePeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TitheResponse {
    private UUID id;
    private String membershipNumber;
    private String memberName;
    private BigDecimal amount;
    private String currency;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private TithePeriod titheMonth;
    private Integer titheYear;
    private String periodLabel;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;
}