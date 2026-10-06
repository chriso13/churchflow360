package com.churchflow360.eventservice.repository;


import com.churchflow360.eventservice.model.ChurchEvent;
import com.churchflow360.eventservice.model.EventStatus;
import com.churchflow360.eventservice.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<ChurchEvent, String> {

    Page<ChurchEvent> findByStatusOrderByEventDateAsc(EventStatus status, Pageable pageable);

    Page<ChurchEvent> findByEventTypeAndStatusOrderByEventDateAsc(
            EventType eventType, EventStatus status, Pageable pageable);

    List<ChurchEvent> findByEventDateBetweenAndStatusOrderByEventDateAscStartTimeAsc(
            LocalDate from, LocalDate to, EventStatus status);

    List<ChurchEvent> findByEventDateAndStatusOrderByStartTimeAsc(
            LocalDate date, EventStatus status);

    List<ChurchEvent> findByOrganizerIdOrderByEventDateDesc(String organizerId);

    // Upcoming events from today onwards
    @Query("{ 'eventDate': { $gte: ?0 }, 'status': 'PUBLISHED' }")
    Page<ChurchEvent> findUpcomingEvents(LocalDate from, Pageable pageable);

    // Full-text style search on title and description
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, " +
            "{ 'description': { $regex: ?0, $options: 'i' } }, " +
            "{ 'tags': { $regex: ?0, $options: 'i' } } ] }")
    Page<ChurchEvent> searchEvents(String keyword, Pageable pageable);

    long countByStatusAndEventDateGreaterThanEqual(EventStatus status, LocalDate date);
}