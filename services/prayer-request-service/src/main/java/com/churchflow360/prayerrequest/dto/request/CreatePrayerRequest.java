package com.churchflow360.prayerrequest.dto.request;

import com.churchflow360.prayerrequest.model.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreatePrayerRequest {
    private String requesterMembershipNumber;
    private String requesterName;

    @NotBlank(message = "Title is required") @Size(max = 255)
    private String title;

    @NotBlank(message = "Request text is required")
    private String requestText;

    private RequestCategory category = RequestCategory.OTHER;
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;
    private UrgencyLevel urgency = UrgencyLevel.NORMAL;
    private Boolean isAnonymous = false;
    private String assignedPastorNumber;
    private Integer expiryDays; // null = no expiry
}
