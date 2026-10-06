package com.churchflow360.pastoralservice.service;

import com.churchflow360.pastoralservice.client.MemberClient;
import com.churchflow360.pastoralservice.dto.request.*;
import com.churchflow360.pastoralservice.dto.response.*;
import com.churchflow360.pastoralservice.exception.*;
import com.churchflow360.pastoralservice.mapper.MeetingMapper;
import com.churchflow360.pastoralservice.model.*;
import com.churchflow360.pastoralservice.repository.ConfidentialNoteRepository;
import com.churchflow360.pastoralservice.repository.PastoralMeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PastoralMeetingService {

    private final PastoralMeetingRepository   meetingRepository;
    private final ConfidentialNoteRepository  noteRepository;
    private final MeetingMapper               meetingMapper;
    private final MemberClient                memberClient;
    private final PastoralNotificationService notificationService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public MeetingResponse requestMeeting(CreateMeetingRequest request) {
        memberClient.verifyMemberExists(request.getPastorMembershipNumber());
        memberClient.verifyMemberExists(request.getMemberMembershipNumber());
        if (!request.getPastorMembershipNumber().equals(request.getRequestedBy())) {
            memberClient.verifyMemberExists(request.getRequestedBy());
        }

        PastoralMeeting meeting = meetingMapper.toEntity(request);
        meeting.setStatus(MeetingStatus.REQUESTED);

        PastoralMeeting saved = meetingRepository.save(meeting);
        log.info("Pastoral meeting requested: pastor={} member={} type={}",
                saved.getPastorMembershipNumber(),
                saved.getMemberMembershipNumber(),
                saved.getMeetingType());

        // Notify member — no email/phone on entity, notify by membership number
        notificationService.notifyMeetingRequested(
                saved.getMemberMembershipNumber(),
                saved.getMemberName(),
                saved.getPastorName(),
                saved.getSubject() != null ? saved.getSubject() : saved.getMeetingType().name(),
                saved.getId().toString());

        return meetingMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public MeetingResponse getMeeting(UUID id) {
        return meetingMapper.toResponse(findOrThrow(id));
    }

    public PagedResponse<MeetingResponse> getMeetingsByPastor(
            String pastorMembershipNumber, MeetingStatus status, Pageable pageable) {
        memberClient.verifyMemberExists(pastorMembershipNumber);

        var page = status != null
                ? meetingRepository.findByPastorMembershipNumberAndStatusOrderByScheduledDateAsc(
                        pastorMembershipNumber.toUpperCase(), status, pageable)
                : meetingRepository.findByPastorMembershipNumberOrderByScheduledDateDesc(
                        pastorMembershipNumber.toUpperCase(), pageable);

        return PagedResponse.of(page.map(meetingMapper::toResponse));
    }

    public PagedResponse<MeetingResponse> getMeetingsByMember(
            String memberMembershipNumber, Pageable pageable) {
        memberClient.verifyMemberExists(memberMembershipNumber);

        return PagedResponse.of(
                meetingRepository.findByMemberMembershipNumberOrderByScheduledDateDesc(
                        memberMembershipNumber.toUpperCase(), pageable)
                        .map(meetingMapper::toResponse));
    }

    public List<MeetingResponse> getPastorScheduleForDate(
            String pastorMembershipNumber, LocalDate date) {
        memberClient.verifyMemberExists(pastorMembershipNumber);
        return meetingRepository
                .findByPastorMembershipNumberAndScheduledDateOrderByScheduledTimeAsc(
                        pastorMembershipNumber.toUpperCase(), date)
                .stream().map(meetingMapper::toResponse).toList();
    }

    public List<MeetingResponse> getPendingRequests(String pastorMembershipNumber) {
        memberClient.verifyMemberExists(pastorMembershipNumber);
        return meetingRepository
                .findPendingRequestsForPastor(pastorMembershipNumber.toUpperCase())
                .stream().map(meetingMapper::toResponse).toList();
    }

    public List<MeetingResponse> getOverdueFollowUps(String pastorMembershipNumber) {
        memberClient.verifyMemberExists(pastorMembershipNumber);
        return meetingRepository
                .findMeetingsWithOverdueFollowUp(
                        pastorMembershipNumber.toUpperCase(), LocalDate.now())
                .stream().map(meetingMapper::toResponse).toList();
    }

    public MeetingStatsResponse getStatsForPastor(String pastorMembershipNumber) {
        memberClient.verifyMemberExists(pastorMembershipNumber);
        String num = pastorMembershipNumber.toUpperCase();

        long pending = noteRepository
                .findByAuthorMembershipNumberAndFollowUpRequiredTrueOrderByFollowUpByAsc(num)
                .stream().filter(n -> n.getFollowUpBy() != null
                        && !n.getFollowUpBy().isAfter(LocalDate.now()))
                .count();

        return MeetingStatsResponse.builder()
                .totalMeetings(meetingRepository
                        .findByPastorMembershipNumberOrderByScheduledDateDesc(num, Pageable.unpaged())
                        .getTotalElements())
                .requested(meetingRepository
                        .countByPastorMembershipNumberAndStatus(num, MeetingStatus.REQUESTED))
                .confirmed(meetingRepository
                        .countByPastorMembershipNumberAndStatus(num, MeetingStatus.CONFIRMED))
                .completed(meetingRepository
                        .countByPastorMembershipNumberAndStatus(num, MeetingStatus.COMPLETED))
                .cancelled(meetingRepository
                        .countByPastorMembershipNumberAndStatus(num, MeetingStatus.CANCELLED))
                .noShow(meetingRepository
                        .countByPastorMembershipNumberAndStatus(num, MeetingStatus.NO_SHOW))
                .rescheduled(meetingRepository
                        .countByPastorMembershipNumberAndStatus(num, MeetingStatus.RESCHEDULED))
                .pendingFollowUp(pending)
                .build();
    }

    // ── Status workflow ───────────────────────────────────────────────────────

    @Transactional
    public MeetingResponse confirmMeeting(UUID id, ConfirmMeetingRequest request) {
        PastoralMeeting meeting = findOrThrow(id);
        validateTransition(meeting, MeetingStatus.CONFIRMED);

        meeting.setStatus(MeetingStatus.CONFIRMED);
        meeting.setScheduledDate(request.getScheduledDate());
        meeting.setScheduledTime(request.getScheduledTime());
        if (request.getLocation() != null)       meeting.setLocation(request.getLocation());
        if (request.getDurationMinutes() != null) meeting.setDurationMinutes(request.getDurationMinutes());

        PastoralMeeting saved = meetingRepository.save(meeting);
        log.info("Meeting confirmed: {} on {} at {}", id, saved.getScheduledDate(), saved.getScheduledTime());

        notificationService.notifyMeetingConfirmed(
                saved.getMemberMembershipNumber(), saved.getMemberName(),
                saved.getPastorMembershipNumber(), saved.getPastorName(),
                saved.getScheduledDate(), saved.getScheduledTime(),
                saved.getLocation(),
                saved.getSubject() != null ? saved.getSubject() : saved.getMeetingType().name(),
                saved.getId().toString());

        return meetingMapper.toResponse(saved);
    }

    @Transactional
    public MeetingResponse rescheduleMeeting(UUID id, RescheduleMeetingRequest request) {
        PastoralMeeting meeting = findOrThrow(id);
        validateTransition(meeting, MeetingStatus.RESCHEDULED);

        meeting.setStatus(MeetingStatus.RESCHEDULED);
        meeting.setRescheduledDate(request.getNewDate());
        meeting.setRescheduledTime(request.getNewTime());
        meeting.setRescheduleReason(request.getReason());
        if (request.getLocation() != null) meeting.setLocation(request.getLocation());

        PastoralMeeting saved = meetingRepository.save(meeting);
        log.info("Meeting rescheduled: {} to {} at {}", id, request.getNewDate(), request.getNewTime());

        notificationService.notifyMeetingRescheduled(
                saved.getMemberMembershipNumber(), saved.getMemberName(),
                saved.getPastorName(),
                saved.getRescheduledDate(), saved.getRescheduledTime(),
                saved.getLocation(), request.getReason(),
                saved.getId().toString());

        return meetingMapper.toResponse(saved);
    }

    @Transactional
    public MeetingResponse cancelMeeting(UUID id, CancelMeetingRequest request) {
        PastoralMeeting meeting = findOrThrow(id);
        validateTransition(meeting, MeetingStatus.CANCELLED);

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setRescheduleReason(request.getReason());

        PastoralMeeting saved = meetingRepository.save(meeting);
        log.info("Meeting cancelled: {} — reason: {}", id, request.getReason());

        notificationService.notifyMeetingCancelled(
                saved.getMemberMembershipNumber(), saved.getMemberName(),
                saved.getPastorName(), request.getReason(),
                saved.getId().toString());

        return meetingMapper.toResponse(saved);
    }

    @Transactional
    public MeetingResponse completeMeeting(UUID id) {
        PastoralMeeting meeting = findOrThrow(id);
        validateTransition(meeting, MeetingStatus.COMPLETED);

        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setCompletedAt(LocalDateTime.now());

        PastoralMeeting saved = meetingRepository.save(meeting);
        log.info("Meeting completed: {}", id);

        notificationService.notifyMeetingCompleted(
                saved.getMemberMembershipNumber(), saved.getMemberName(),
                saved.getPastorName(), saved.getId().toString());

        return meetingMapper.toResponse(saved);
    }

    @Transactional
    public MeetingResponse markNoShow(UUID id) {
        PastoralMeeting meeting = findOrThrow(id);
        validateTransition(meeting, MeetingStatus.NO_SHOW);

        meeting.setStatus(MeetingStatus.NO_SHOW);
        PastoralMeeting saved = meetingRepository.save(meeting);
        log.info("Meeting marked as no-show: {}", id);

        notificationService.notifyMeetingNoShow(
                saved.getMemberMembershipNumber(), saved.getMemberName(),
                saved.getPastorName(), saved.getScheduledDate(),
                saved.getId().toString());

        return meetingMapper.toResponse(saved);
    }

    // ── Confidential Notes ────────────────────────────────────────────────────

    @Transactional
    public ConfidentialNoteResponse addNote(UUID meetingId, CreateNoteRequest request) {
        PastoralMeeting meeting = findOrThrow(meetingId);

        if (!meeting.getPastorMembershipNumber()
                .equalsIgnoreCase(request.getAuthorMembershipNumber())) {
            throw new UnauthorizedException(
                    "Only the assigned pastor (" + meeting.getPastorMembershipNumber() +
                    ") can add confidential notes to this meeting.");
        }

        memberClient.verifyMemberExists(request.getAuthorMembershipNumber());

        ConfidentialNote note = meetingMapper.toEntity(request);
        note.setMeeting(meeting);

        ConfidentialNote saved = noteRepository.save(note);
        log.info("Confidential note added to meeting {} by pastor {}",
                meetingId, request.getAuthorMembershipNumber());
        return meetingMapper.toResponse(saved);
    }

    public List<ConfidentialNoteResponse> getNotes(UUID meetingId, String requesterMembershipNumber) {
        PastoralMeeting meeting = findOrThrow(meetingId);

        if (!meeting.getPastorMembershipNumber()
                .equalsIgnoreCase(requesterMembershipNumber)) {
            throw new UnauthorizedException(
                    "Access denied. Confidential notes are only visible to the assigned pastor.");
        }

        return noteRepository.findByMeetingIdOrderByCreatedAtDesc(meetingId)
                .stream().map(meetingMapper::toResponse).toList();
    }

    @Transactional
    public void deleteNote(UUID noteId, String requesterMembershipNumber) {
        ConfidentialNote note = noteRepository
                .findByIdAndAuthorMembershipNumber(noteId, requesterMembershipNumber.toUpperCase())
                .orElseThrow(() -> new UnauthorizedException(
                        "Note not found or you do not have permission to delete it."));
        noteRepository.delete(note);
        log.info("Confidential note {} deleted by {}", noteId, requesterMembershipNumber);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PastoralMeeting findOrThrow(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pastoral meeting not found with id: " + id));
    }

    private void validateTransition(PastoralMeeting meeting, MeetingStatus targetStatus) {
        if (!meeting.canTransitionTo(targetStatus)) {
            throw new MeetingOperationException(
                    "Cannot transition meeting from " + meeting.getStatus() +
                    " to " + targetStatus + ". " +
                    "Valid transitions from " + meeting.getStatus() + ": " +
                    getAllowedTransitions(meeting.getStatus()));
        }
    }

    private String getAllowedTransitions(MeetingStatus from) {
        return switch (from) {
            case REQUESTED   -> "CONFIRMED, CANCELLED";
            case CONFIRMED   -> "RESCHEDULED, COMPLETED, CANCELLED, NO_SHOW";
            case RESCHEDULED -> "CONFIRMED, CANCELLED";
            default          -> "none (terminal state)";
        };
    }
}
