package com.churchflow360.eventservice.dto.request;

import com.churchflow360.eventservice.model.RsvpStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RsvpRequest {

    @NotBlank(message = "Membership number is required")
    @Pattern(
            regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format. Expected: MEM-MMDD-YYYY-HHMMSS"
    )
    private String membershipNumber;

    private String memberName;
    private String memberEmail;

    @NotNull(message = "RSVP status is required")
    private RsvpStatus status;

    @Min(value = 0, message = "Guest count cannot be negative")
    private int guestCount;

    private String notes;
}