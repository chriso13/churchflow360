package com.churchflow360.financeservice.dto.request;

import com.churchflow360.financeservice.model.PaymentMethod;
import com.churchflow360.financeservice.model.TithePeriod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordTitheRequest {

    @NotBlank(message = "Membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format")
    private String membershipNumber;

    @NotBlank(message = "Member name is required")
    private String memberName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String referenceNumber;

    @NotNull(message = "Tithe month is required")
    private TithePeriod titheMonth;

    @NotNull(message = "Tithe year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer titheYear;

    private String notes;

    @NotBlank(message = "Recorded by membership number is required")
    private String recordedBy;
}