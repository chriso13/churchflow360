package com.churchflow360.financeservice.dto.request;

import com.churchflow360.financeservice.model.OfferingType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RecordOfferingRequest {

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    private String serviceDescription;

    @NotNull(message = "Offering type is required")
    private OfferingType offeringType;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalAmount;

    private String currency;

    @DecimalMin(value = "0.00")
    private BigDecimal cashAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal checkAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal digitalAmount;

    @Min(value = 0)
    private Integer giverCount;

    private String countedBy;
    private String notes;

    @NotBlank(message = "Recorded by is required")
    private String recordedBy;

    // Optional: add individual items at creation time
    private List<AddOfferingItemRequest> items;
}