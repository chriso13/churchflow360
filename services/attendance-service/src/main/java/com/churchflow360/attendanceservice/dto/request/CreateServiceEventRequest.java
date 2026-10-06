package com.churchflow360.attendanceservice.dto.request;

import com.churchflow360.attendanceservice.model.ServiceType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateServiceEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String description;
}