package com.churchflow360.smallgroups.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_memberships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "membership_number"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupMembership {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SmallGroup group;

    @Column(name = "membership_number", nullable = false, length = 50)
    private String membershipNumber;

    @Column(name = "member_name", length = 255)
    private String memberName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "member_role")
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "joined_date")
    @Builder.Default
    private LocalDate joinedDate = LocalDate.now();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
