package com.churchflow360.pastoralservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Confidential pastoral notes — STRICTLY pastor-only.
 *
 * <p>These are NEVER included in regular meeting responses.
 * They are only accessible via a dedicated secured endpoint:
 * {@code GET /api/v1/pastoral-meetings/{id}/notes}
 *
 * <p>Business rules:
 * <ul>
 *   <li>Only the pastor assigned to the meeting can create notes.</li>
 *   <li>Only the author pastor can read their own notes.</li>
 *   <li>Notes are soft-protected — in production add role-based security.</li>
 * </ul>
 */
@Entity
@Table(name = "confidential_notes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfidentialNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private PastoralMeeting meeting;

    @Column(name = "author_membership_number", nullable = false, length = 50)
    private String authorMembershipNumber;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "follow_up_required")
    @Builder.Default
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_by")
    private LocalDate followUpBy;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}