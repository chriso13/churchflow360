package com.churchflow360.attendanceservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckInRequest {

    @NotNull(message = "Service event ID is required")
    private UUID serviceEventId;

    @NotBlank(message = "Membership number is required")
    @Pattern(
            regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format. Expected: MEM-MMDD-YYYY-HHMMSS"
    )
    private String membershipNumber;

    private String memberName;
    private String notes;
}