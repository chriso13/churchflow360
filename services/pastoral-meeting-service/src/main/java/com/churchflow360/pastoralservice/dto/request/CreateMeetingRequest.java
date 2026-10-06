package com.churchflow360.pastoralservice.dto.request;

import com.churchflow360.pastoralservice.model.MeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateMeetingRequest {

    @NotBlank(message = "Pastor membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format. Expected: MEM-MMDD-YYYY-HHMMSS")
    private String pastorMembershipNumber;

    private String pastorName;

    @NotBlank(message = "Member membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format. Expected: MEM-MMDD-YYYY-HHMMSS")
    private String memberMembershipNumber;

    private String memberName;

    @NotNull(message = "Meeting type is required")
    private MeetingType meetingType;

    // ── Member's preferred date/time (what they're asking for) ─────────────────
    // The pastor reviews these and sets the official scheduledDate/Time on confirm.
    private LocalDate preferredDate;
    private LocalTime preferredTime;

    // ── Pastor-set schedule (usually null at request time) ─────────────────────
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;

    @Positive(message = "Duration must be a positive number of minutes")
    private Integer durationMinutes;

    private String location;
    private String subject;
    private String requestNotes;

    @NotBlank(message = "Requested by membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format")
    private String requestedBy;
}