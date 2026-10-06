package com.churchflow360.eventservice.service;

import com.churchflow360.eventservice.client.MemberClient;
import com.churchflow360.eventservice.dto.request.CreateEventRequest;
import com.churchflow360.eventservice.dto.request.RsvpRequest;
import com.churchflow360.eventservice.dto.request.UpdateEventRequest;
import com.churchflow360.eventservice.dto.response.*;
import com.churchflow360.eventservice.exception.EventOperationException;
import com.churchflow360.eventservice.exception.ResourceNotFoundException;
import com.churchflow360.eventservice.kafka.EventKafkaPublisher;
import com.churchflow360.eventservice.mapper.EventMapper;
import com.churchflow360.eventservice.model.*;
import com.churchflow360.eventservice.repository.EventRepository;
import com.churchflow360.eventservice.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository    eventRepository;
    private final RsvpRepository     rsvpRepository;
    private final EventMapper        eventMapper;
    private final EventKafkaPublisher kafkaPublisher;
    private final MemberClient       memberClient;

    // ── CRUD ───────────────────────────────────────────────────────────────────

    /**
     * Creates a new event in DRAFT status.
     * Verifies the organizer membership number exists in member-service.
     */
    public EventResponse createEvent(CreateEventRequest request) {
        // Verify organizer exists in member-service
        if (request.getOrganizerId() != null && !request.getOrganizerId().isBlank()) {
            memberClient.verifyMemberExists(request.getOrganizerId());
        }

        ChurchEvent event = eventMapper.toEntity(request);
        event.setStatus(EventStatus.DRAFT);
        ChurchEvent saved = eventRepository.save(event);
        log.info("Event created: {} [{}]", saved.getTitle(), saved.getId());
        return enrichWithCounts(saved);
    }

    public EventResponse getEvent(String id) {
        return enrichWithCounts(findOrThrow(id));
    }

    /**
     * Updates an event.
     * If organizerId is being changed, verifies the new organizer exists.
     */
    public EventResponse updateEvent(String id, UpdateEventRequest request) {
        ChurchEvent event = findOrThrow(id);

        if (event.isCancelled()) {
            throw new EventOperationException("Cannot update a cancelled event.");
        }

        // Verify new organizer exists if organizerId is being changed
        if (request.getOrganizerId() != null && !request.getOrganizerId().isBlank()) {
            memberClient.verifyMemberExists(request.getOrganizerId());
        }

        eventMapper.updateEntity(request, event);
        ChurchEvent saved = eventRepository.save(event);
        kafkaPublisher.publishEventUpdated(saved);
        log.info("Event updated: {}", saved.getId());
        return enrichWithCounts(saved);
    }

    public EventResponse publishEvent(String id) {
        ChurchEvent event = findOrThrow(id);

        if (event.isCancelled()) {
            throw new EventOperationException("Cannot publish a cancelled event.");
        }
        if (event.isPublished()) {
            throw new EventOperationException("Event is already published.");
        }

        event.setStatus(EventStatus.PUBLISHED);
        ChurchEvent saved = eventRepository.save(event);
        kafkaPublisher.publishEventCreated(saved);
        log.info("Event published: {}", saved.getId());
        return enrichWithCounts(saved);
    }

    public EventResponse cancelEvent(String id) {
        ChurchEvent event = findOrThrow(id);

        if (event.isCancelled()) {
            throw new EventOperationException("Event is already cancelled.");
        }

        event.setStatus(EventStatus.CANCELLED);
        ChurchEvent saved = eventRepository.save(event);
        kafkaPublisher.publishEventCancelled(saved);
        log.info("Event cancelled: {}", saved.getId());
        return enrichWithCounts(saved);
    }

    public EventResponse completeEvent(String id) {
        ChurchEvent event = findOrThrow(id);
        event.setStatus(EventStatus.COMPLETED);
        return enrichWithCounts(eventRepository.save(event));
    }

    public void deleteEvent(String id) {
        ChurchEvent event = findOrThrow(id);
        rsvpRepository.deleteByEventId(id);
        eventRepository.delete(event);
        log.info("Event deleted: {}", id);
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    public PagedResponse<EventResponse> getPublishedEvents(Pageable pageable) {
        Page<ChurchEvent> page = eventRepository.findByStatusOrderByEventDateAsc(
                EventStatus.PUBLISHED, pageable);
        return PagedResponse.of(page.map(this::enrichWithCounts));
    }

    public PagedResponse<EventResponse> getUpcomingEvents(Pageable pageable) {
        Page<ChurchEvent> page = eventRepository.findUpcomingEvents(LocalDate.now(), pageable);
        return PagedResponse.of(page.map(this::enrichWithCounts));
    }

    public PagedResponse<EventResponse> getEventsByType(EventType type, Pageable pageable) {
        Page<ChurchEvent> page = eventRepository.findByEventTypeAndStatusOrderByEventDateAsc(
                type, EventStatus.PUBLISHED, pageable);
        return PagedResponse.of(page.map(this::enrichWithCounts));
    }

    public List<EventResponse> getEventsByDate(LocalDate date) {
        return eventRepository.findByEventDateAndStatusOrderByStartTimeAsc(
                        date, EventStatus.PUBLISHED)
                .stream().map(this::enrichWithCounts).toList();
    }

    public List<EventResponse> getEventsByDateRange(LocalDate from, LocalDate to) {
        return eventRepository.findByEventDateBetweenAndStatusOrderByEventDateAscStartTimeAsc(
                        from, to, EventStatus.PUBLISHED)
                .stream().map(this::enrichWithCounts).toList();
    }

    public PagedResponse<EventResponse> searchEvents(String keyword, Pageable pageable) {
        Page<ChurchEvent> page = eventRepository.searchEvents(keyword, pageable);
        return PagedResponse.of(page.map(this::enrichWithCounts));
    }

    // ── RSVP ──────────────────────────────────────────────────────────────────

    /**
     * Submits or updates an RSVP.
     * Verifies the membership number exists in member-service before saving.
     */
    public RsvpResponse submitRsvp(String eventId, RsvpRequest request) {
        ChurchEvent event = findOrThrow(eventId);

        if (event.isCancelled()) {
            throw new EventOperationException(
                    "Cannot RSVP to a cancelled event: " + event.getTitle());
        }
        if (!event.isPublished()) {
            throw new EventOperationException("This event is not yet open for RSVPs.");
        }

        // Verify the member exists in member-service before accepting the RSVP
        memberClient.verifyMemberExists(request.getMembershipNumber());

        // Check capacity — only count ATTENDING responses
        if (event.getCapacity() > 0
                && RsvpStatus.ATTENDING.equals(request.getStatus())) {
            long attending = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.ATTENDING);
            if (attending >= event.getCapacity()) {
                throw new EventOperationException(
                        "This event has reached maximum capacity (" + event.getCapacity() + ").");
            }
        }

        // Upsert — update existing RSVP or create new one
        Rsvp rsvp = rsvpRepository
                .findByEventIdAndMembershipNumber(
                        eventId, request.getMembershipNumber().toUpperCase())
                .orElse(new Rsvp());

        rsvp.setEventId(eventId);
        rsvp.setEventTitle(event.getTitle());
        rsvp.setMembershipNumber(request.getMembershipNumber().toUpperCase());
        rsvp.setMemberName(request.getMemberName());
        rsvp.setMemberEmail(request.getMemberEmail());
        rsvp.setStatus(request.getStatus());
        rsvp.setGuestCount(request.getGuestCount());
        rsvp.setNotes(request.getNotes());

        Rsvp saved = rsvpRepository.save(rsvp);
        kafkaPublisher.publishRsvpSubmitted(saved);

        log.info("RSVP submitted: {} → {} for event {}",
                request.getMembershipNumber(), request.getStatus(), event.getTitle());
        return eventMapper.toResponse(saved);
    }

    public List<RsvpResponse> getEventRsvps(String eventId) {
        findOrThrow(eventId);
        return rsvpRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream().map(eventMapper::toResponse).toList();
    }

    public List<RsvpResponse> getAttendingRsvps(String eventId) {
        findOrThrow(eventId);
        return rsvpRepository
                .findByEventIdAndStatusOrderByCreatedAtAsc(eventId, RsvpStatus.ATTENDING)
                .stream().map(eventMapper::toResponse).toList();
    }

    /**
     * Gets a specific member's RSVP.
     * Verifies membership number exists before querying.
     */
    public RsvpResponse getMemberRsvp(String eventId, String membershipNumber) {
        findOrThrow(eventId);
        memberClient.verifyMemberExists(membershipNumber);

        Rsvp rsvp = rsvpRepository
                .findByEventIdAndMembershipNumber(eventId, membershipNumber.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No RSVP found for member " + membershipNumber + " in this event."));
        return eventMapper.toResponse(rsvp);
    }

    /**
     * Gets all RSVPs for a member across all events.
     * Verifies membership number exists before querying.
     */
    public PagedResponse<RsvpResponse> getMemberRsvpHistory(
            String membershipNumber, Pageable pageable) {
        memberClient.verifyMemberExists(membershipNumber);

        return PagedResponse.of(
                rsvpRepository.findByMembershipNumberOrderByCreatedAtDesc(
                                membershipNumber.toUpperCase(), pageable)
                        .map(eventMapper::toResponse));
    }

    public EventStatsResponse getEventStats(String eventId) {
        ChurchEvent event = findOrThrow(eventId);

        long attending    = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.ATTENDING);
        long notAttending = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.NOT_ATTENDING);
        long maybe        = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.MAYBE);
        long total        = rsvpRepository.countByEventId(eventId);

        long totalGuests = rsvpRepository
                .findByEventIdAndStatusOrderByCreatedAtAsc(eventId, RsvpStatus.ATTENDING)
                .stream().mapToLong(Rsvp::getGuestCount).sum();

        return EventStatsResponse.builder()
                .totalRsvps(total)
                .attending(attending)
                .notAttending(notAttending)
                .maybe(maybe)
                .totalGuests(totalGuests)
                .capacity(event.getCapacity())
                .capacityReached(event.getCapacity() > 0 && attending >= event.getCapacity())
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ChurchEvent findOrThrow(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + id));
    }

    private EventResponse enrichWithCounts(ChurchEvent event) {
        EventResponse response = eventMapper.toResponse(event);
        response.setTotalRsvps(rsvpRepository.countByEventId(event.getId()));
        response.setAttendingCount(
                rsvpRepository.countByEventIdAndStatus(event.getId(), RsvpStatus.ATTENDING));
        return response;
    }
}