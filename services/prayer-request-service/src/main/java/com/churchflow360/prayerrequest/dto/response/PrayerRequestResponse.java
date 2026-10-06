package com.churchflow360.prayerrequest.dto.response;

import com.churchflow360.prayerrequest.model.*;
import lombok.Data;
import java.time.*;
import java.util.UUID;

@Data
public class PrayerRequestResponse {
    private UUID id;
    private String requesterMembershipNumber;
    private String requesterName;
    private String title;
    private String requestText;
    private RequestCategory category;
    private PrivacyLevel privacyLevel;
    private RequestStatus status;
    private UrgencyLevel urgency;
    private Boolean isAnonymous;
    private Integer prayerCount;
    private String answeredTestimony;
    private LocalDateTime answeredAt;
    private String assignedPastorNumber;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
