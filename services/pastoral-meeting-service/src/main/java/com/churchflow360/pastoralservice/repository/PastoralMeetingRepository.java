package com.churchflow360.pastoralservice.repository;

import com.churchflow360.pastoralservice.model.MeetingStatus;
import com.churchflow360.pastoralservice.model.PastoralMeeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PastoralMeetingRepository extends JpaRepository<PastoralMeeting, UUID> {

    // Pastor's meetings
    Page<PastoralMeeting> findByPastorMembershipNumberOrderByScheduledDateDesc(
            String pastorMembershipNumber, Pageable pageable);

    Page<PastoralMeeting> findByPastorMembershipNumberAndStatusOrderByScheduledDateAsc(
            String pastorMembershipNumber, MeetingStatus status, Pageable pageable);

    // Member's meetings
    Page<PastoralMeeting> findByMemberMembershipNumberOrderByScheduledDateDesc(
            String memberMembershipNumber, Pageable pageable);

    // Status-based queries
    Page<PastoralMeeting> findByStatusOrderByScheduledDateAsc(
            MeetingStatus status, Pageable pageable);

    // Upcoming meetings for a pastor on a date
    List<PastoralMeeting> findByPastorMembershipNumberAndScheduledDateOrderByScheduledTimeAsc(
            String pastorMembershipNumber, LocalDate date);

    // Pending requests (REQUESTED status — not yet confirmed)
    @Query("""
            SELECT m FROM PastoralMeeting m
            WHERE m.pastorMembershipNumber = :pastor
            AND m.status = 'REQUESTED'
            ORDER BY m.createdAt ASC
            """)
    List<PastoralMeeting> findPendingRequestsForPastor(@Param("pastor") String pastorMembershipNumber);

    // Meetings needing follow-up (completed but follow_up_required in notes)
    @Query(value = """
            SELECT DISTINCT pm.* FROM pastoral_meetings pm
            JOIN confidential_notes cn ON cn.meeting_id = pm.id
            WHERE cn.follow_up_required = true
            AND cn.follow_up_by <= :today
            AND cn.author_membership_number = :pastor
            """, nativeQuery = true)
    List<PastoralMeeting> findMeetingsWithOverdueFollowUp(
            @Param("pastor") String pastorMembershipNumber,
            @Param("today") LocalDate today);

    long countByPastorMembershipNumberAndStatus(String pastorMembershipNumber, MeetingStatus status);

    long countByMemberMembershipNumberAndStatus(String memberMembershipNumber, MeetingStatus status);
}