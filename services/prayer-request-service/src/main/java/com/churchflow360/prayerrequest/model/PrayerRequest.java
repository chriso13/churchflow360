package com.churchflow360.prayerrequest.model;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "prayer_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrayerRequest {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "requester_membership_number", length = 50)
    private String requesterMembershipNumber; // null = anonymous

    @Column(name = "requester_name", length = 255)
    private String requesterName;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "request_text", nullable = false, columnDefinition = "TEXT")
    private String requestText;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category", nullable = false, columnDefinition = "request_category")
    @Builder.Default private RequestCategory category = RequestCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "privacy_level", nullable = false, columnDefinition = "privacy_level")
    @Builder.Default private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "request_status")
    @Builder.Default private RequestStatus status = RequestStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "urgency", nullable = false, columnDefinition = "urgency_level")
    @Builder.Default private UrgencyLevel urgency = UrgencyLevel.NORMAL;

    @Column(name = "is_anonymous") @Builder.Default
    private Boolean isAnonymous = false;

    @Column(name = "prayer_count") @Builder.Default
    private Integer prayerCount = 0;

    @Column(name = "answered_testimony", columnDefinition = "TEXT")
    private String answeredTestimony;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "assigned_pastor_number", length = 50)
    private String assignedPastorNumber;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
