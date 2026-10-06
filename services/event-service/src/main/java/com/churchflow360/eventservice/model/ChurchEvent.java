package com.churchflow360.eventservice.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Church event stored in MongoDB.
 *
 * <p>MongoDB is used here because events have highly variable schemas —
 * a baptism event needs different fields than a conference or retreat.
 * The flexible document model handles all types without schema migrations.
 */
@Document(collection = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChurchEvent {

    @Id
    private String id;

    private String title;
    private String description;

    private EventType eventType;
    private EventStatus status;

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String location;
    private String address;
    private String onlineLink;       // for hybrid / online events

    private int capacity;            // 0 = unlimited
    private boolean rsvpRequired;
    private boolean publicEvent;     // visible without login

    private String organizerId;      // membershipNumber of the organizer
    private String organizerName;
    private String contactEmail;
    private String contactPhone;

    // Tags for filtering (e.g. "worship", "kids", "leadership")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // Image or banner URL
    private String bannerImage;

    @Indexed                         // MongoDB index — fast event lookup by date
    private LocalDate indexDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── Helpers ────────────────────────────────────────────────────────────────

    public boolean isCancelled() {
        return EventStatus.CANCELLED.equals(this.status);
    }

    public boolean isPublished() {
        return EventStatus.PUBLISHED.equals(this.status);
    }
}