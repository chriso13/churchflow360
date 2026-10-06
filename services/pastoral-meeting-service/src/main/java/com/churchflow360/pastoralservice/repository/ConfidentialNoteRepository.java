package com.churchflow360.pastoralservice.repository;

import com.churchflow360.pastoralservice.model.ConfidentialNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfidentialNoteRepository extends JpaRepository<ConfidentialNote, UUID> {

    List<ConfidentialNote> findByMeetingIdOrderByCreatedAtDesc(UUID meetingId);

    Optional<ConfidentialNote> findByIdAndAuthorMembershipNumber(UUID id, String authorMembershipNumber);

    List<ConfidentialNote> findByAuthorMembershipNumberAndFollowUpRequiredTrueOrderByFollowUpByAsc(
            String authorMembershipNumber);
}