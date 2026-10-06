package com.churchflow360.prayerrequest.repository;

import com.churchflow360.prayerrequest.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface PrayerRequestRepository extends JpaRepository<PrayerRequest, UUID> {
    Page<PrayerRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable p);
    Page<PrayerRequest> findByCategoryOrderByCreatedAtDesc(RequestCategory category, Pageable p);
    Page<PrayerRequest> findByRequesterMembershipNumberOrderByCreatedAtDesc(String mn, Pageable p);
    Page<PrayerRequest> findByPrivacyLevelInOrderByCreatedAtDesc(List<PrivacyLevel> levels, Pageable p);
    Page<PrayerRequest> findByStatusAndPrivacyLevelInOrderByCreatedAtDesc(
            RequestStatus status, List<PrivacyLevel> levels, Pageable p);
    long countByStatus(RequestStatus status);
    long countByCategory(RequestCategory category);

    @Query("SELECT r FROM PrayerRequest r WHERE r.urgency = 'URGENT' AND r.status = 'ACTIVE' ORDER BY r.createdAt DESC")
    List<PrayerRequest> findUrgentActive();
}
