package com.churchflow360.announcement.dto.request;

import com.churchflow360.announcement.model.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateAnnouncementRequest {

    @NotBlank(message = "Title is required") @Size(max = 255)
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500)
    private String summary;

    private AnnouncementCategory category = AnnouncementCategory.GENERAL;
    private AudienceType audience = AudienceType.ALL_MEMBERS;
    private Priority priority = Priority.NORMAL;
    private UUID targetGroupId;

    @NotBlank(message = "Author membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$", message = "Invalid membership number format")
    private String authorMembershipNumber;

    private String authorName;
    private String imageUrl;
    private String actionUrl;
    private String actionLabel;
    private Boolean pinToTop = false;
    private LocalDateTime expiresAt;
    private LocalDateTime scheduledAt; // if set, status = SCHEDULED
}
