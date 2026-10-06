package com.churchflow360.eventservice.dto.request;

import com.churchflow360.eventservice.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String location;
    private String address;
    private String onlineLink;

    @PositiveOrZero(message = "Capacity must be 0 (unlimited) or a positive number")
    private int capacity;

    private boolean rsvpRequired;
    private boolean publicEvent;

    private String organizerId;
    private String organizerName;
    private String contactEmail;
    private String contactPhone;

    private List<String> tags;
    private String bannerImage;
}