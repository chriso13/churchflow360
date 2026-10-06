package com.churchflow360.financeservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offerings")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "service_description", length = 255)
    private String serviceDescription;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "offering_type", nullable = false, columnDefinition = "offering_type")
    @Builder.Default
    private OfferingType offeringType = OfferingType.SUNDAY_OFFERING;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "cash_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "check_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal checkAmount = BigDecimal.ZERO;

    @Column(name = "digital_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal digitalAmount = BigDecimal.ZERO;

    @Column(name = "giver_count", nullable = false)
    @Builder.Default
    private Integer giverCount = 0;

    @Column(name = "counted_by", length = 255)
    private String countedBy;

    @Column(name = "verified_by", length = 255)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by", nullable = false, length = 50)
    private String recordedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "offering", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<OfferingItem> items = new ArrayList<>();

    public boolean isVerified() { return verifiedAt != null; }

    public void recalculateTotal() {
        this.totalAmount = cashAmount.add(checkAmount).add(digitalAmount);
    }
}