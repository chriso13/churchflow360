package com.churchflow360.financeservice.dto.response;

import com.churchflow360.financeservice.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OfferingItemResponse {
    private UUID id;
    private String membershipNumber;
    private String giverName;
    private Boolean isAnonymous;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String envelopeNumber;
    private String notes;
    private LocalDateTime createdAt;
}