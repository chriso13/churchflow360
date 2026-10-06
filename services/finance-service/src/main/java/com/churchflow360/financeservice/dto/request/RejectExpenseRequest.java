package com.churchflow360.financeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RejectExpenseRequest {

    @NotBlank(message = "Rejector membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "rejectedBy: invalid format. Expected MEM-MMDD-YYYY-HHMMSS")
    private String rejectedBy;

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}