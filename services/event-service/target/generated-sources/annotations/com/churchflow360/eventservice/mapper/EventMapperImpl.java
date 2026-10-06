package com.churchflow360.eventservice.mapper;

import com.churchflow360.eventservice.dto.request.CreateEventRequest;
import com.churchflow360.eventservice.dto.request.RsvpRequest;
import com.churchflow360.eventservice.dto.request.UpdateEventRequest;
import com.churchflow360.eventservice.dto.response.EventResponse;
import com.churchflow360.eventservice.dto.response.RsvpResponse;
import com.churchflow360.eventservice.model.ChurchEvent;
import com.churchflow360.eventservice.model.Rsvp;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-28T22:51:59-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public ChurchEvent toEntity(CreateEventRequest request) {
        if ( request == null ) {
            return null;
        }

        ChurchEvent.ChurchEventBuilder churchEvent = ChurchEvent.builder();

        churchEvent.indexDate( request.getEventDate() );
        churchEvent.title( request.getTitle() );
        churchEvent.description( request.getDescription() );
        churchEvent.eventType( request.getEventType() );
        churchEvent.eventDate( request.getEventDate() );
        churchEvent.startTime( request.getStartTime() );
        churchEvent.endTime( request.getEndTime() );
        churchEvent.location( request.getLocation() );
        churchEvent.address( request.getAddress() );
        churchEvent.onlineLink( request.getOnlineLink() );
        churchEvent.capacity( request.getCapacity() );
        churchEvent.rsvpRequired( request.isRsvpRequired() );
        churchEvent.publicEvent( request.isPublicEvent() );
        churchEvent.organizerId( request.getOrganizerId() );
        churchEvent.organizerName( request.getOrganizerName() );
        churchEvent.contactEmail( request.getContactEmail() );
        churchEvent.contactPhone( request.getContactPhone() );
        List<String> list = request.getTags();
        if ( list != null ) {
            churchEvent.tags( new ArrayList<String>( list ) );
        }
        churchEvent.bannerImage( request.getBannerImage() );

        churchEvent.status( com.churchflow360.eventservice.model.EventStatus.DRAFT );

        return churchEvent.build();
    }

    @Override
    public void updateEntity(UpdateEventRequest request, ChurchEvent event) {
        if ( request == null ) {
            return;
        }

        if ( request.getEventDate() != null ) {
            event.setIndexDate( request.getEventDate() );
        }
        if ( request.getTitle() != null ) {
            event.setTitle( request.getTitle() );
        }
        if ( request.getDescription() != null ) {
            event.setDescription( request.getDescription() );
        }
        if ( request.getEventType() != null ) {
            event.setEventType( request.getEventType() );
        }
        if ( request.getEventDate() != null ) {
            event.setEventDate( request.getEventDate() );
        }
        if ( request.getStartTime() != null ) {
            event.setStartTime( request.getStartTime() );
        }
        if ( request.getEndTime() != null ) {
            event.setEndTime( request.getEndTime() );
        }
        if ( request.getLocation() != null ) {
            event.setLocation( request.getLocation() );
        }
        if ( request.getAddress() != null ) {
            event.setAddress( request.getAddress() );
        }
        if ( request.getOnlineLink() != null ) {
            event.setOnlineLink( request.getOnlineLink() );
        }
        if ( request.getCapacity() != null ) {
            event.setCapacity( request.getCapacity() );
        }
        if ( request.getRsvpRequired() != null ) {
            event.setRsvpRequired( request.getRsvpRequired() );
        }
        if ( request.getPublicEvent() != null ) {
            event.setPublicEvent( request.getPublicEvent() );
        }
        if ( request.getOrganizerId() != null ) {
            event.setOrganizerId( request.getOrganizerId() );
        }
        if ( request.getOrganizerName() != null ) {
            event.setOrganizerName( request.getOrganizerName() );
        }
        if ( request.getContactEmail() != null ) {
            event.setContactEmail( request.getContactEmail() );
        }
        if ( request.getContactPhone() != null ) {
            event.setContactPhone( request.getContactPhone() );
        }
        if ( event.getTags() != null ) {
            List<String> list = request.getTags();
            if ( list != null ) {
                event.getTags().clear();
                event.getTags().addAll( list );
            }
        }
        else {
            List<String> list = request.getTags();
            if ( list != null ) {
                event.setTags( new ArrayList<String>( list ) );
            }
        }
        if ( request.getBannerImage() != null ) {
            event.setBannerImage( request.getBannerImage() );
        }
    }

    @Override
    public EventResponse toResponse(ChurchEvent event) {
        if ( event == null ) {
            return null;
        }

        EventResponse eventResponse = new EventResponse();

        eventResponse.setId( event.getId() );
        eventResponse.setTitle( event.getTitle() );
        eventResponse.setDescription( event.getDescription() );
        eventResponse.setEventType( event.getEventType() );
        eventResponse.setStatus( event.getStatus() );
        eventResponse.setEventDate( event.getEventDate() );
        eventResponse.setStartTime( event.getStartTime() );
        eventResponse.setEndTime( event.getEndTime() );
        eventResponse.setLocation( event.getLocation() );
        eventResponse.setAddress( event.getAddress() );
        eventResponse.setOnlineLink( event.getOnlineLink() );
        eventResponse.setCapacity( event.getCapacity() );
        eventResponse.setRsvpRequired( event.isRsvpRequired() );
        eventResponse.setPublicEvent( event.isPublicEvent() );
        eventResponse.setOrganizerId( event.getOrganizerId() );
        eventResponse.setOrganizerName( event.getOrganizerName() );
        eventResponse.setContactEmail( event.getContactEmail() );
        eventResponse.setContactPhone( event.getContactPhone() );
        List<String> list = event.getTags();
        if ( list != null ) {
            eventResponse.setTags( new ArrayList<String>( list ) );
        }
        eventResponse.setBannerImage( event.getBannerImage() );
        eventResponse.setCreatedAt( event.getCreatedAt() );
        eventResponse.setUpdatedAt( event.getUpdatedAt() );

        return eventResponse;
    }

    @Override
    public Rsvp toEntity(RsvpRequest request) {
        if ( request == null ) {
            return null;
        }

        Rsvp.RsvpBuilder rsvp = Rsvp.builder();

        rsvp.membershipNumber( request.getMembershipNumber() );
        rsvp.memberName( request.getMemberName() );
        rsvp.memberEmail( request.getMemberEmail() );
        rsvp.status( request.getStatus() );
        rsvp.guestCount( request.getGuestCount() );
        rsvp.notes( request.getNotes() );

        return rsvp.build();
    }

    @Override
    public RsvpResponse toResponse(Rsvp rsvp) {
        if ( rsvp == null ) {
            return null;
        }

        RsvpResponse rsvpResponse = new RsvpResponse();

        rsvpResponse.setId( rsvp.getId() );
        rsvpResponse.setEventId( rsvp.getEventId() );
        rsvpResponse.setEventTitle( rsvp.getEventTitle() );
        rsvpResponse.setMembershipNumber( rsvp.getMembershipNumber() );
        rsvpResponse.setMemberName( rsvp.getMemberName() );
        rsvpResponse.setMemberEmail( rsvp.getMemberEmail() );
        rsvpResponse.setStatus( rsvp.getStatus() );
        rsvpResponse.setGuestCount( rsvp.getGuestCount() );
        rsvpResponse.setNotes( rsvp.getNotes() );
        rsvpResponse.setCreatedAt( rsvp.getCreatedAt() );
        rsvpResponse.setUpdatedAt( rsvp.getUpdatedAt() );

        return rsvpResponse;
    }
}
