package com.churchflow360.pastoralservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RescheduleMeetingRequest {

    @NotNull(message = "New date is required")
    private LocalDate newDate;

    @NotNull(message = "New time is required")
    private LocalTime newTime;

    @NotBlank(message = "Reason for rescheduling is required")
    private String reason;

    private String location;
}