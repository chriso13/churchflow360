package com.churchflow360.financeservice.dto.response;

import com.churchflow360.financeservice.model.OfferingType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OfferingResponse {
    private UUID id;
    private LocalDate serviceDate;
    private String serviceDescription;
    private OfferingType offeringType;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal cashAmount;
    private BigDecimal checkAmount;
    private BigDecimal digitalAmount;
    private Integer giverCount;
    private String countedBy;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private boolean verified;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;
    private List<OfferingItemResponse> items;
}