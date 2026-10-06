package com.churchflow360.eventservice.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * RSVP response — one document per (eventId, membershipNumber) pair.
 *
 * Uniqueness is enforced by the init-mongo.js script which creates the
 * compound unique index directly on the collection at container startup.
 * The @CompoundIndex annotation is intentionally removed to avoid the
 * "IndexOptionsConflict" error caused when Spring Data tries to re-create
 * an index that already exists under a different auto-generated name.
 */
@Document(collection = "rsvps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rsvp {

    @Id
    private String id;

    @Indexed
    private String eventId;

    private String eventTitle;

    @Indexed
    private String membershipNumber;

    private String memberName;
    private String memberEmail;

    private RsvpStatus status;

    private int guestCount;
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}