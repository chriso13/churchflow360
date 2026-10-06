package com.churchflow360.prayerrequest.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class AnswerPrayerRequest {
    @NotBlank(message = "Testimony or note is required")
    private String testimony;
}
