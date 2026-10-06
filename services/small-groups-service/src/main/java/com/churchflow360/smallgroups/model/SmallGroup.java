package com.churchflow360.smallgroups.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "small_groups")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SmallGroup {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "group_type", nullable = false, columnDefinition = "group_type")
    @Builder.Default
    private GroupType groupType = GroupType.CELL_GROUP;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "group_status")
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "meeting_frequency", columnDefinition = "meeting_frequency")
    private MeetingFrequency meetingFrequency;

    @Column(name = "meeting_day", length = 20)
    private String meetingDay;  // MONDAY, TUESDAY, etc.

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Column(name = "meeting_location", length = 255)
    private String meetingLocation;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 20;

    @Column(name = "leader_membership_number", nullable = false, length = 50)
    private String leaderMembershipNumber;

    @Column(name = "leader_name", length = 255)
    private String leaderName;

    @Column(name = "target_area", length = 150)
    private String targetArea;  // geographic or demographic focus

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GroupMembership> memberships = new ArrayList<>();
}
