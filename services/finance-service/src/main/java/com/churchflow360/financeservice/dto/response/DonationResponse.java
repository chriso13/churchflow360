package com.churchflow360.financeservice.dto.response;

import com.churchflow360.financeservice.model.DonationCategory;
import com.churchflow360.financeservice.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DonationResponse {
    private UUID id;
    private String membershipNumber;
    private String donorName;
    private String donorEmail;
    private BigDecimal amount;
    private String currency;
    private DonationCategory category;
    private String fundName;
    private Boolean isAnonymous;
    private Boolean isRecurring;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String receiptNumber;
    private Boolean receiptIssued;
    private LocalDateTime receiptIssuedAt;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;
}