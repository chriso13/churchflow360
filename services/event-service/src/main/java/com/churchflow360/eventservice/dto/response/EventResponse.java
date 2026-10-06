package com.churchflow360.eventservice.dto.response;

import com.churchflow360.eventservice.model.EventStatus;
import com.churchflow360.eventservice.model.EventType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class EventResponse {
    private String id;
    private String title;
    private String description;
    private EventType eventType;
    private EventStatus status;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String address;
    private String onlineLink;
    private int capacity;
    private boolean rsvpRequired;
    private boolean publicEvent;
    private String organizerId;
    private String organizerName;
    private String contactEmail;
    private String contactPhone;
    private List<String> tags;
    private String bannerImage;
    private long totalRsvps;
    private long attendingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}