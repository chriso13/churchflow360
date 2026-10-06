package com.churchflow360.pastoralservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pastoral_meetings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastoralMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── People ─────────────────────────────────────────────────────────────────

    @Column(name = "pastor_membership_number", nullable = false, length = 50)
    private String pastorMembershipNumber;

    @Column(name = "pastor_name", length = 255)
    private String pastorName;

    @Column(name = "member_membership_number", nullable = false, length = 50)
    private String memberMembershipNumber;

    @Column(name = "member_name", length = 255)
    private String memberName;

    // ── Meeting details ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "meeting_type", nullable = false, columnDefinition = "meeting_type")
    @Builder.Default
    private MeetingType meetingType = MeetingType.GENERAL_CARE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "meeting_status")
    @Builder.Default
    private MeetingStatus status = MeetingStatus.REQUESTED;

    // ── Member's preferred date/time (set at request time) ─────────────────────
    // These hold what the MEMBER asked for. The pastor then sets the official
    // scheduledDate/scheduledTime when confirming (which may differ).

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "preferred_time")
    private LocalTime preferredTime;

    // ── Pastor's confirmed schedule ────────────────────────────────────────────

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 60;

    @Column(length = 255)
    private String location;

    @Column(length = 500)
    private String subject;

    @Column(name = "request_notes", columnDefinition = "TEXT")
    private String requestNotes;

    // ── Reschedule ─────────────────────────────────────────────────────────────

    @Column(name = "rescheduled_date")
    private LocalDate rescheduledDate;

    @Column(name = "rescheduled_time")
    private LocalTime rescheduledTime;

    @Column(name = "reschedule_reason", columnDefinition = "TEXT")
    private String rescheduleReason;

    // ── Completion ─────────────────────────────────────────────────────────────

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @Column(name = "requested_by", nullable = false, length = 50)
    private String requestedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Confidential notes (never in meeting response) ─────────────────────────

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConfidentialNote> confidentialNotes = new ArrayList<>();

    // ── Helpers ────────────────────────────────────────────────────────────────

    public boolean canTransitionTo(MeetingStatus newStatus) {
        return switch (this.status) {
            case REQUESTED   -> newStatus == MeetingStatus.CONFIRMED
                    || newStatus == MeetingStatus.CANCELLED;
            case CONFIRMED   -> newStatus == MeetingStatus.RESCHEDULED
                    || newStatus == MeetingStatus.COMPLETED
                    || newStatus == MeetingStatus.CANCELLED
                    || newStatus == MeetingStatus.NO_SHOW;
            case RESCHEDULED -> newStatus == MeetingStatus.CONFIRMED
                    || newStatus == MeetingStatus.CANCELLED;
            case COMPLETED,
                 CANCELLED,
                 NO_SHOW     -> false;
        };
    }
}