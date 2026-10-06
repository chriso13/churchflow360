package com.churchflow360.pastoralservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ConfirmMeetingRequest {

    @NotNull(message = "Scheduled date is required to confirm")
    private LocalDate scheduledDate;

    @NotNull(message = "Scheduled time is required to confirm")
    private LocalTime scheduledTime;

    private String location;
    private Integer durationMinutes;
}