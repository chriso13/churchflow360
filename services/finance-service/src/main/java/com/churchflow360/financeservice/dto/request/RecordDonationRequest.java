package com.churchflow360.financeservice.dto.request;

import com.churchflow360.financeservice.model.DonationCategory;
import com.churchflow360.financeservice.model.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordDonationRequest {

    // Optional — null means anonymous / non-member donor
    private String membershipNumber;

    private String donorName;

    @Email(message = "Invalid email address")
    private String donorEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Category is required")
    private DonationCategory category;

    private String fundName;

    private Boolean isAnonymous;
    private Boolean isRecurring;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String referenceNumber;
    private String notes;

    @NotBlank(message = "Recorded by is required")
    private String recordedBy;
}