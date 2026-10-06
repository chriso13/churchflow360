package com.churchflow360.eventservice.service;

import com.churchflow360.eventservice.dto.request.CreateEventRequest;
import com.churchflow360.eventservice.dto.request.RsvpRequest;
import com.churchflow360.eventservice.dto.response.EventResponse;
import com.churchflow360.eventservice.dto.response.RsvpResponse;
import com.churchflow360.eventservice.exception.EventOperationException;
import com.churchflow360.eventservice.exception.ResourceNotFoundException;
import com.churchflow360.eventservice.kafka.EventKafkaPublisher;
import com.churchflow360.eventservice.mapper.EventMapper;
import com.churchflow360.eventservice.model.*;
import com.churchflow360.eventservice.repository.EventRepository;
import com.churchflow360.eventservice.repository.RsvpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService unit tests")
class EventServiceTest {

    @Mock EventRepository eventRepository;
    @Mock RsvpRepository rsvpRepository;
    @Mock EventMapper eventMapper;
    @Mock EventKafkaPublisher kafkaPublisher;

    @InjectMocks EventService eventService;

    private ChurchEvent event;
    private final String EVENT_ID = "64f1a2b3c4d5e6f7a8b9c0d1";
    private final String MEMBERSHIP_NUMBER = "MEM-0609-2026-113045";

    @BeforeEach
    void setUp() {
        event = new ChurchEvent();
        event.setId(EVENT_ID);
        event.setTitle("Sunday Service");
        event.setEventType(EventType.SUNDAY_SERVICE);
        event.setEventDate(LocalDate.now().plusDays(7));
        event.setStatus(EventStatus.DRAFT);
        event.setCapacity(0);
    }

    @Test
    @DisplayName("createEvent saves event with DRAFT status")
    void createEvent_savesDraft() {
        CreateEventRequest req = new CreateEventRequest();
        req.setTitle("Sunday Service");
        req.setEventType(EventType.SUNDAY_SERVICE);
        req.setEventDate(LocalDate.now().plusDays(7));

        when(eventMapper.toEntity(req)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(new EventResponse());
        when(rsvpRepository.countByEventId(any())).thenReturn(0L);
        when(rsvpRepository.countByEventIdAndStatus(any(), any())).thenReturn(0L);

        eventService.createEvent(req);

        verify(eventRepository).save(argThat(e -> e.getStatus() == EventStatus.DRAFT));
        verify(kafkaPublisher, never()).publishEventCreated(any());
    }

    @Test
    @DisplayName("publishEvent transitions to PUBLISHED and publishes Kafka event")
    void publishEvent_transitionsAndPublishes() {
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);
        when(eventMapper.toResponse(any(ChurchEvent.class))).thenReturn(new EventResponse());
        when(rsvpRepository.countByEventId(any())).thenReturn(0L);
        when(rsvpRepository.countByEventIdAndStatus(any(), any())).thenReturn(0L);

        eventService.publishEvent(EVENT_ID);

        verify(kafkaPublisher).publishEventCreated(any());
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    @DisplayName("publishEvent throws when event is already cancelled")
    void publishEvent_throwsWhenCancelled() {
        event.setStatus(EventStatus.CANCELLED);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publishEvent(EVENT_ID))
                .isInstanceOf(EventOperationException.class)
                .hasMessageContaining("Cannot publish a cancelled event");
    }

    @Test
    @DisplayName("cancelEvent transitions to CANCELLED and publishes Kafka event")
    void cancelEvent_transitionsAndPublishes() {
        event.setStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);
        when(eventMapper.toResponse(any(ChurchEvent.class))).thenReturn(new EventResponse());
        when(rsvpRepository.countByEventId(any())).thenReturn(0L);
        when(rsvpRepository.countByEventIdAndStatus(any(), any())).thenReturn(0L);

        eventService.cancelEvent(EVENT_ID);

        verify(kafkaPublisher).publishEventCancelled(any());
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    @DisplayName("submitRsvp throws when event is cancelled")
    void submitRsvp_throwsWhenCancelled() {
        event.setStatus(EventStatus.CANCELLED);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

        RsvpRequest req = new RsvpRequest();
        req.setMembershipNumber(MEMBERSHIP_NUMBER);
        req.setStatus(RsvpStatus.ATTENDING);

        assertThatThrownBy(() -> eventService.submitRsvp(EVENT_ID, req))
                .isInstanceOf(EventOperationException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("submitRsvp enforces capacity limit")
    void submitRsvp_throwsWhenCapacityReached() {
        event.setStatus(EventStatus.PUBLISHED);
        event.setCapacity(10);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
        when(rsvpRepository.countByEventIdAndStatus(EVENT_ID, RsvpStatus.ATTENDING))
                .thenReturn(10L);

        RsvpRequest req = new RsvpRequest();
        req.setMembershipNumber(MEMBERSHIP_NUMBER);
        req.setStatus(RsvpStatus.ATTENDING);

        assertThatThrownBy(() -> eventService.submitRsvp(EVENT_ID, req))
                .isInstanceOf(EventOperationException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    @DisplayName("submitRsvp saves and publishes Kafka event")
    void submitRsvp_savesAndPublishes() {
        event.setStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByEventIdAndMembershipNumber(any(), any()))
                .thenReturn(Optional.empty());
        when(rsvpRepository.countByEventIdAndStatus(any(), any())).thenReturn(0L);
        Rsvp rsvp = new Rsvp();
        when(rsvpRepository.save(any())).thenReturn(rsvp);
        when(eventMapper.toResponse(rsvp)).thenReturn(new RsvpResponse());

        RsvpRequest req = new RsvpRequest();
        req.setMembershipNumber(MEMBERSHIP_NUMBER);
        req.setMemberName("James Anderson");
        req.setStatus(RsvpStatus.ATTENDING);

        eventService.submitRsvp(EVENT_ID, req);

        verify(rsvpRepository).save(any(Rsvp.class));
        verify(kafkaPublisher).publishRsvpSubmitted(any());
    }

    @Test
    @DisplayName("getEvent throws ResourceNotFoundException when not found")
    void getEvent_notFound() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent("non-existent-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}