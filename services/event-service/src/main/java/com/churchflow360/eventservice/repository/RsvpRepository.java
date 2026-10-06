package com.churchflow360.eventservice.repository;

import com.churchflow360.eventservice.model.Rsvp;
import com.churchflow360.eventservice.model.RsvpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RsvpRepository extends MongoRepository<Rsvp, String> {

    Optional<Rsvp> findByEventIdAndMembershipNumber(String eventId, String membershipNumber);

    boolean existsByEventIdAndMembershipNumber(String eventId, String membershipNumber);

    List<Rsvp> findByEventIdAndStatusOrderByCreatedAtAsc(String eventId, RsvpStatus status);

    List<Rsvp> findByEventIdOrderByCreatedAtAsc(String eventId);

    Page<Rsvp> findByMembershipNumberOrderByCreatedAtDesc(String membershipNumber, Pageable pageable);

    long countByEventId(String eventId);

    long countByEventIdAndStatus(String eventId, RsvpStatus status);

    void deleteByEventId(String eventId);
}