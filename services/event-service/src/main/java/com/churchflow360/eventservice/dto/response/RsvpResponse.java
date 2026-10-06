package com.churchflow360.eventservice.dto.response;

import com.churchflow360.eventservice.model.RsvpStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RsvpResponse {
    private String id;
    private String eventId;
    private String eventTitle;
    private String membershipNumber;
    private String memberName;
    private String memberEmail;
    private RsvpStatus status;
    private int guestCount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}