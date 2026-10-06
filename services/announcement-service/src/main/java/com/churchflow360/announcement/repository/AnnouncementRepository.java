package com.churchflow360.announcement.repository;

import com.churchflow360.announcement.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    Page<Announcement> findByStatusOrderByPinToTopDescCreatedAtDesc(AnnouncementStatus status, Pageable p);
    Page<Announcement> findByStatusAndAudienceInOrderByPinToTopDescCreatedAtDesc(
            AnnouncementStatus status, List<AudienceType> audiences, Pageable p);
    Page<Announcement> findByCategoryAndStatusOrderByCreatedAtDesc(
            AnnouncementCategory category, AnnouncementStatus status, Pageable p);
    Page<Announcement> findByAuthorMembershipNumberOrderByCreatedAtDesc(String mn, Pageable p);

    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' " +
           "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "AND a.audience IN :audiences ORDER BY a.pinToTop DESC, a.createdAt DESC")
    Page<Announcement> findActive(@Param("now") LocalDateTime now,
                                  @Param("audiences") List<AudienceType> audiences, Pageable p);

    @Query("SELECT a FROM Announcement a WHERE a.status = 'SCHEDULED' AND a.scheduledAt <= :now")
    List<Announcement> findDueForPublishing(@Param("now") LocalDateTime now);

    List<Announcement> findByStatusAndPriorityOrderByCreatedAtDesc(
            AnnouncementStatus status, Priority priority);

    long countByStatus(AnnouncementStatus status);
}
