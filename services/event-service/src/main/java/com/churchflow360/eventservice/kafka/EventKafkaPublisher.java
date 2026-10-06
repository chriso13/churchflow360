package com.churchflow360.eventservice.kafka;

import com.churchflow360.eventservice.model.ChurchEvent;
import com.churchflow360.eventservice.model.Rsvp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishes church event and RSVP domain events to Kafka.
 *
 * <p>These events are consumed by the Notification Service which routes
 * them to the appropriate channel (email, SMS, push) based on the topic
 * and member preferences.
 *
 * <p>Topics:
 * <ul>
 *   <li>{@code event.created}   → notify all members about a new event</li>
 *   <li>{@code event.updated}   → notify members who RSVPd about changes</li>
 *   <li>{@code event.cancelled} → notify all RSVPd members of cancellation</li>
 *   <li>{@code event.rsvp}      → confirm RSVP to the member who submitted it</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${churchflow360.kafka.topics.event-created}")
    private String eventCreatedTopic;

    @Value("${churchflow360.kafka.topics.event-updated}")
    private String eventUpdatedTopic;

    @Value("${churchflow360.kafka.topics.event-cancelled}")
    private String eventCancelledTopic;

    @Value("${churchflow360.kafka.topics.event-rsvp}")
    private String eventRsvpTopic;

    // ── Event lifecycle events ─────────────────────────────────────────────────

    public void publishEventCreated(ChurchEvent event) {
        Map<String, Object> payload = buildEventPayload("EVENT_CREATED", event);
        payload.put("action", "A new event has been published: " + event.getTitle());
        send(eventCreatedTopic, event.getId(), payload);
    }

    public void publishEventUpdated(ChurchEvent event) {
        Map<String, Object> payload = buildEventPayload("EVENT_UPDATED", event);
        payload.put("action", "Event details have been updated: " + event.getTitle());
        send(eventUpdatedTopic, event.getId(), payload);
    }

    public void publishEventCancelled(ChurchEvent event) {
        Map<String, Object> payload = buildEventPayload("EVENT_CANCELLED", event);
        payload.put("action", "Event has been cancelled: " + event.getTitle());
        payload.put("reason", "The event organiser has cancelled this event.");
        send(eventCancelledTopic, event.getId(), payload);
    }

    // ── RSVP events ────────────────────────────────────────────────────────────

    public void publishRsvpSubmitted(Rsvp rsvp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",        "EVENT_RSVP");
        payload.put("eventId",          rsvp.getEventId());
        payload.put("eventTitle",       rsvp.getEventTitle());
        payload.put("membershipNumber", rsvp.getMembershipNumber());
        payload.put("memberName",       rsvp.getMemberName());
        payload.put("memberEmail",      rsvp.getMemberEmail() != null ? rsvp.getMemberEmail() : "");
        payload.put("rsvpStatus",       rsvp.getStatus().name());
        payload.put("guestCount",       rsvp.getGuestCount());
        payload.put("occurredAt",       LocalDateTime.now().toString());
        send(eventRsvpTopic, rsvp.getEventId(), payload);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Map<String, Object> buildEventPayload(String eventType, ChurchEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",    eventType);
        payload.put("eventId",      event.getId());
        payload.put("title",        event.getTitle());
        payload.put("eventDate",    event.getEventDate().toString());
        payload.put("location",     event.getLocation() != null ? event.getLocation() : "");
        payload.put("organizerId",  event.getOrganizerId() != null ? event.getOrganizerId() : "");
        payload.put("occurredAt",   LocalDateTime.now().toString());
        return payload;
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish to topic {}: {}", topic, ex.getMessage());
                    } else {
                        log.debug("Published to topic {} partition {} offset {}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}