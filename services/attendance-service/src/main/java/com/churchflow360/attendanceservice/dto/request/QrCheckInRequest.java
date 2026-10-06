package com.churchflow360.attendanceservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QrCheckInRequest {

    @NotBlank(message = "QR token is required")
    private String qrToken;

    @NotBlank(message = "Membership number is required")
    @Pattern(
            regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format. Expected: MEM-MMDD-YYYY-HHMMSS"
    )
    private String membershipNumber;

    private String memberName;
}