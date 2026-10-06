package com.churchflow360.financeservice.dto.request;

import com.churchflow360.financeservice.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MarkPaidRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentReference;

    private LocalDate paidDate;

    private String notes;
}