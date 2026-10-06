package com.churchflow360.eventservice.dto.request;

import com.churchflow360.eventservice.model.EventType;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class UpdateEventRequest {

    private String title;
    private String description;
    private EventType eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String address;
    private String onlineLink;

    @PositiveOrZero
    private Integer capacity;

    private Boolean rsvpRequired;
    private Boolean publicEvent;
    private String organizerId;
    private String organizerName;
    private String contactEmail;
    private String contactPhone;
    private List<String> tags;
    private String bannerImage;
}