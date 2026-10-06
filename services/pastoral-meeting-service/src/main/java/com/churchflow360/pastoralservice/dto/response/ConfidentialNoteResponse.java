package com.churchflow360.pastoralservice.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Confidential note response — returned ONLY from the secured /notes endpoint.
 */
@Data
public class ConfidentialNoteResponse {
    private UUID id;
    private UUID meetingId;
    private String authorMembershipNumber;
    private String authorName;
    private String content;
    private Boolean followUpRequired;
    private LocalDate followUpBy;
    private String followUpNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}