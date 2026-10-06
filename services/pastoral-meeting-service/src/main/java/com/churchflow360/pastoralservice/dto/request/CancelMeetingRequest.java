package com.churchflow360.pastoralservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelMeetingRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}