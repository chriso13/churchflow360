package com.churchflow360.eventservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventStatsResponse {
    private long totalRsvps;
    private long attending;
    private long notAttending;
    private long maybe;
    private long totalGuests;
    private int capacity;
    private boolean capacityReached;
}