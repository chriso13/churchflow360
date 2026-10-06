package com.churchflow360.eventservice.mapper;

import com.churchflow360.eventservice.dto.request.CreateEventRequest;
import com.churchflow360.eventservice.dto.request.RsvpRequest;
import com.churchflow360.eventservice.dto.request.UpdateEventRequest;
import com.churchflow360.eventservice.dto.response.EventResponse;
import com.churchflow360.eventservice.dto.response.RsvpResponse;
import com.churchflow360.eventservice.model.ChurchEvent;
import com.churchflow360.eventservice.model.EventStatus;
import com.churchflow360.eventservice.model.Rsvp;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "indexDate", source = "eventDate")
    @Mapping(target = "status",    expression = "java(com.churchflow360.eventservice.model.EventStatus.DRAFT)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ChurchEvent toEntity(CreateEventRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "indexDate", source = "eventDate")
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateEventRequest request, @MappingTarget ChurchEvent event);

    // totalRsvps and attendingCount are populated by the service layer
    @Mapping(target = "totalRsvps",     ignore = true)
    @Mapping(target = "attendingCount", ignore = true)
    EventResponse toResponse(ChurchEvent event);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "eventId",   ignore = true)
    @Mapping(target = "eventTitle",ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rsvp toEntity(RsvpRequest request);

    RsvpResponse toResponse(Rsvp rsvp);
}