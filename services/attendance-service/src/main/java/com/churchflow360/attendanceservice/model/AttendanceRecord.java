package com.churchflow360.attendanceservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_attendance_member_service",
                columnNames = {"service_event_id", "membership_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_event_id", nullable = false)
    private ServiceEvent serviceEvent;

    // Membership number from member-service (e.g. MEM-0609-2026-113045)
    // Stored directly so attendance works even without calling member-service
    // and reports show the number even if the member record is later changed.
    @Column(name = "membership_number", nullable = false, length = 50)
    private String membershipNumber;

    // Denormalized member name so reports don't require a member-service call
    @Column(name = "member_name", length = 255)
    private String memberName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "checkin_method", nullable = false, columnDefinition = "checkin_method")
    @Builder.Default
    private CheckInMethod checkinMethod = CheckInMethod.MANUAL;

    @Column(name = "checked_in_at", nullable = false)
    @Builder.Default
    private LocalDateTime checkedInAt = LocalDateTime.now();

    @Column(name = "checked_in_by")
    private UUID checkedInBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}