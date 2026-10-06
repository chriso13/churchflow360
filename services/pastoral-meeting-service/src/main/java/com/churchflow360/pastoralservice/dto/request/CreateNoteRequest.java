package com.churchflow360.pastoralservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateNoteRequest {

    @NotBlank(message = "Author membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "Invalid membership number format")
    private String authorMembershipNumber;

    private String authorName;

    @NotBlank(message = "Note content is required")
    private String content;

    private Boolean followUpRequired;
    private LocalDate followUpBy;
    private String followUpNotes;
}