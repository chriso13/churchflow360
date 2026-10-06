package com.churchflow360.memberservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Personal info ──────────────────────────────────────────────────────────

    @Column(unique = true)
    private String membershipNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // @JdbcTypeCode(SqlTypes.NAMED_ENUM) tells Hibernate 6 to send this value
    // as a PostgreSQL named ENUM type (casting VARCHAR → gender) instead of
    // plain VARCHAR which causes: "column is of type gender but expression
    // is of type character varying"
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", columnDefinition = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "marital_status", columnDefinition = "marital_status")
    private MaritalStatus maritalStatus;

    // ── Address ────────────────────────────────────────────────────────────────

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(length = 100)
    @Builder.Default
    private String country = "United States";

    // ── Profile ────────────────────────────────────────────────────────────────

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(length = 150)
    private String occupation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Membership ─────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "membership_status", nullable = false, columnDefinition = "member_status")
    @Builder.Default
    private MemberStatus membershipStatus = MemberStatus.VISITOR;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "baptism_date")
    private LocalDate baptismDate;

    @Column(name = "water_baptized")
    @Builder.Default
    private Boolean waterBaptized = false;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Helpers ────────────────────────────────────────────────────────────────

    public String getFullName() {
        return firstName + " " + lastName;
    }
}