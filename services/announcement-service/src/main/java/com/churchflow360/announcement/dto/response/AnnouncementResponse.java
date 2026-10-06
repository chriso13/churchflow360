package com.churchflow360.announcement.dto.response;

import com.churchflow360.announcement.model.*;
import lombok.Data;
import java.time.*;
import java.util.UUID;

@Data
public class AnnouncementResponse {
    private UUID id;
    private String title;
    private String content;
    private String summary;
    private AnnouncementCategory category;
    private AudienceType audience;
    private AnnouncementStatus status;
    private Priority priority;
    private UUID targetGroupId;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime scheduledAt;
    private String authorMembershipNumber;
    private String authorName;
    private String imageUrl;
    private String actionUrl;
    private String actionLabel;
    private Integer viewCount;
    private Boolean pinToTop;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
