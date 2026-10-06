package com.churchflow360.financeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ApproveExpenseRequest {

    @NotBlank(message = "Approver membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "approvedBy: invalid format. Expected MEM-MMDD-YYYY-HHMMSS")
    private String approvedBy;

    private String notes;
}