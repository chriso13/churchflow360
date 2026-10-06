package com.churchflow360.pastoralservice.mapper;

import com.churchflow360.pastoralservice.dto.request.CreateMeetingRequest;
import com.churchflow360.pastoralservice.dto.request.CreateNoteRequest;
import com.churchflow360.pastoralservice.dto.response.ConfidentialNoteResponse;
import com.churchflow360.pastoralservice.dto.response.MeetingResponse;
import com.churchflow360.pastoralservice.model.ConfidentialNote;
import com.churchflow360.pastoralservice.model.MeetingStatus;
import com.churchflow360.pastoralservice.model.PastoralMeeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MeetingMapper {

    // preferredDate and preferredTime are auto-mapped by name (same field name
    // in CreateMeetingRequest and PastoralMeeting) — no explicit @Mapping needed.
    @Mapping(target = "id",                   ignore = true)
    @Mapping(target = "status",               expression = "java(com.churchflow360.pastoralservice.model.MeetingStatus.REQUESTED)")
    @Mapping(target = "rescheduledDate",      ignore = true)
    @Mapping(target = "rescheduledTime",      ignore = true)
    @Mapping(target = "rescheduleReason",     ignore = true)
    @Mapping(target = "completedAt",          ignore = true)
    @Mapping(target = "confidentialNotes",    ignore = true)
    @Mapping(target = "createdAt",            ignore = true)
    @Mapping(target = "updatedAt",            ignore = true)
    PastoralMeeting toEntity(CreateMeetingRequest request);

    // confidentialNotes are intentionally excluded — never in the public response
    // (MeetingResponse has no confidentialNotes property; notes are loaded via separate endpoint)
    // preferredDate and preferredTime are auto-mapped by name.
    MeetingResponse toResponse(PastoralMeeting meeting);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "meeting",         ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "followUpRequired", expression = "java(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : Boolean.FALSE)")
    ConfidentialNote toEntity(CreateNoteRequest request);

    @Mapping(target = "meetingId", source = "meeting.id")
    ConfidentialNoteResponse toResponse(ConfidentialNote note);
}