package com.churchflow360.pastoralservice.mapper;

import com.churchflow360.pastoralservice.dto.request.CreateMeetingRequest;
import com.churchflow360.pastoralservice.dto.request.CreateNoteRequest;
import com.churchflow360.pastoralservice.dto.response.ConfidentialNoteResponse;
import com.churchflow360.pastoralservice.dto.response.MeetingResponse;
import com.churchflow360.pastoralservice.model.ConfidentialNote;
import com.churchflow360.pastoralservice.model.PastoralMeeting;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T19:32:06-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class MeetingMapperImpl implements MeetingMapper {

    @Override
    public PastoralMeeting toEntity(CreateMeetingRequest request) {
        if ( request == null ) {
            return null;
        }

        PastoralMeeting.PastoralMeetingBuilder pastoralMeeting = PastoralMeeting.builder();

        pastoralMeeting.pastorMembershipNumber( request.getPastorMembershipNumber() );
        pastoralMeeting.pastorName( request.getPastorName() );
        pastoralMeeting.memberMembershipNumber( request.getMemberMembershipNumber() );
        pastoralMeeting.memberName( request.getMemberName() );
        pastoralMeeting.meetingType( request.getMeetingType() );
        pastoralMeeting.preferredDate( request.getPreferredDate() );
        pastoralMeeting.preferredTime( request.getPreferredTime() );
        pastoralMeeting.scheduledDate( request.getScheduledDate() );
        pastoralMeeting.scheduledTime( request.getScheduledTime() );
        pastoralMeeting.durationMinutes( request.getDurationMinutes() );
        pastoralMeeting.location( request.getLocation() );
        pastoralMeeting.subject( request.getSubject() );
        pastoralMeeting.requestNotes( request.getRequestNotes() );
        pastoralMeeting.requestedBy( request.getRequestedBy() );

        pastoralMeeting.status( com.churchflow360.pastoralservice.model.MeetingStatus.REQUESTED );

        return pastoralMeeting.build();
    }

    @Override
    public MeetingResponse toResponse(PastoralMeeting meeting) {
        if ( meeting == null ) {
            return null;
        }

        MeetingResponse meetingResponse = new MeetingResponse();

        meetingResponse.setId( meeting.getId() );
        meetingResponse.setPastorMembershipNumber( meeting.getPastorMembershipNumber() );
        meetingResponse.setPastorName( meeting.getPastorName() );
        meetingResponse.setMemberMembershipNumber( meeting.getMemberMembershipNumber() );
        meetingResponse.setMemberName( meeting.getMemberName() );
        meetingResponse.setMeetingType( meeting.getMeetingType() );
        meetingResponse.setStatus( meeting.getStatus() );
        meetingResponse.setPreferredDate( meeting.getPreferredDate() );
        meetingResponse.setPreferredTime( meeting.getPreferredTime() );
        meetingResponse.setScheduledDate( meeting.getScheduledDate() );
        meetingResponse.setScheduledTime( meeting.getScheduledTime() );
        meetingResponse.setDurationMinutes( meeting.getDurationMinutes() );
        meetingResponse.setLocation( meeting.getLocation() );
        meetingResponse.setSubject( meeting.getSubject() );
        meetingResponse.setRequestNotes( meeting.getRequestNotes() );
        meetingResponse.setRescheduledDate( meeting.getRescheduledDate() );
        meetingResponse.setRescheduledTime( meeting.getRescheduledTime() );
        meetingResponse.setRescheduleReason( meeting.getRescheduleReason() );
        meetingResponse.setCompletedAt( meeting.getCompletedAt() );
        meetingResponse.setRequestedBy( meeting.getRequestedBy() );
        meetingResponse.setCreatedAt( meeting.getCreatedAt() );
        meetingResponse.setUpdatedAt( meeting.getUpdatedAt() );

        return meetingResponse;
    }

    @Override
    public ConfidentialNote toEntity(CreateNoteRequest request) {
        if ( request == null ) {
            return null;
        }

        ConfidentialNote.ConfidentialNoteBuilder confidentialNote = ConfidentialNote.builder();

        confidentialNote.authorMembershipNumber( request.getAuthorMembershipNumber() );
        confidentialNote.authorName( request.getAuthorName() );
        confidentialNote.content( request.getContent() );
        confidentialNote.followUpBy( request.getFollowUpBy() );
        confidentialNote.followUpNotes( request.getFollowUpNotes() );

        confidentialNote.followUpRequired( request.getFollowUpRequired() != null ? request.getFollowUpRequired() : Boolean.FALSE );

        return confidentialNote.build();
    }

    @Override
    public ConfidentialNoteResponse toResponse(ConfidentialNote note) {
        if ( note == null ) {
            return null;
        }

        ConfidentialNoteResponse confidentialNoteResponse = new ConfidentialNoteResponse();

        confidentialNoteResponse.setMeetingId( noteMeetingId( note ) );
        confidentialNoteResponse.setId( note.getId() );
        confidentialNoteResponse.setAuthorMembershipNumber( note.getAuthorMembershipNumber() );
        confidentialNoteResponse.setAuthorName( note.getAuthorName() );
        confidentialNoteResponse.setContent( note.getContent() );
        confidentialNoteResponse.setFollowUpRequired( note.getFollowUpRequired() );
        confidentialNoteResponse.setFollowUpBy( note.getFollowUpBy() );
        confidentialNoteResponse.setFollowUpNotes( note.getFollowUpNotes() );
        confidentialNoteResponse.setCreatedAt( note.getCreatedAt() );
        confidentialNoteResponse.setUpdatedAt( note.getUpdatedAt() );

        return confidentialNoteResponse;
    }

    private UUID noteMeetingId(ConfidentialNote confidentialNote) {
        if ( confidentialNote == null ) {
            return null;
        }
        PastoralMeeting meeting = confidentialNote.getMeeting();
        if ( meeting == null ) {
            return null;
        }
        UUID id = meeting.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
