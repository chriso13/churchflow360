package com.churchflow360.pastoralservice.dto.response;

import com.churchflow360.pastoralservice.model.MeetingStatus;
import com.churchflow360.pastoralservice.model.MeetingType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Public meeting response — NEVER contains confidential notes.
 * Notes are returned separately via the secured /notes endpoint.
 */
@Data
public class MeetingResponse {
    private UUID id;
    private String pastorMembershipNumber;
    private String pastorName;
    private String memberMembershipNumber;
    private String memberName;
    private MeetingType meetingType;
    private MeetingStatus status;

    // Member's preferred date/time (what they asked for)
    private LocalDate preferredDate;
    private LocalTime preferredTime;

    // Pastor's confirmed schedule
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;

    private Integer durationMinutes;
    private String location;
    private String subject;
    private String requestNotes;
    private LocalDate rescheduledDate;
    private LocalTime rescheduledTime;
    private String rescheduleReason;
    private LocalDateTime completedAt;
    private String requestedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}