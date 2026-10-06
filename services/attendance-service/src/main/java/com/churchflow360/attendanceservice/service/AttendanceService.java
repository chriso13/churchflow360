package com.churchflow360.attendanceservice.service;

import com.churchflow360.attendanceservice.dto.request.CheckInRequest;
import com.churchflow360.attendanceservice.dto.request.CreateServiceEventRequest;
import com.churchflow360.attendanceservice.dto.request.QrCheckInRequest;
import com.churchflow360.attendanceservice.dto.response.*;
import com.churchflow360.attendanceservice.exception.DuplicateCheckInException;
import com.churchflow360.attendanceservice.exception.LocationConflictException;
import com.churchflow360.attendanceservice.exception.QrTokenExpiredException;
import com.churchflow360.attendanceservice.exception.ResourceNotFoundException;
import com.churchflow360.attendanceservice.mapper.AttendanceMapper;
import com.churchflow360.attendanceservice.model.AttendanceRecord;
import com.churchflow360.attendanceservice.model.CheckInMethod;
import com.churchflow360.attendanceservice.model.ServiceEvent;
import com.churchflow360.attendanceservice.repository.AttendanceRepository;
import com.churchflow360.attendanceservice.repository.ServiceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttendanceService {

    private final ServiceEventRepository serviceEventRepository;
    private final AttendanceRepository   attendanceRepository;
    private final AttendanceMapper       attendanceMapper;
    private final QrCodeService          qrCodeService;

    @Value("${churchflow360.attendance.qr.expiry-minutes:30}")
    private int qrExpiryMinutes;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    // ── Service Events ─────────────────────────────────────────────────────────

    @Transactional
    public ServiceEventResponse createServiceEvent(CreateServiceEventRequest request) {

        // Validate location availability only when location + times are provided
        if (request.getLocation() != null && !request.getLocation().isBlank()
                && request.getStartTime() != null && request.getEndTime() != null) {
            assertLocationAvailable(
                    request.getLocation(),
                    request.getServiceDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    null   // null = new event, no exclusion needed
            );
        }

        ServiceEvent event = attendanceMapper.toEntity(request);
        ServiceEvent saved = serviceEventRepository.save(event);
        log.info("Service event created: {} on {} at {} [{}–{}]",
                saved.getTitle(), saved.getServiceDate(), saved.getLocation(),
                saved.getStartTime(), saved.getEndTime());
        return attendanceMapper.toResponse(saved);
    }

    public ServiceEventResponse getServiceEvent(UUID id) {
        return attendanceMapper.toResponse(findEventOrThrow(id));
    }

    public List<ServiceEventResponse> getServicesByDate(LocalDate date) {
        return serviceEventRepository.findByServiceDateOrderByStartTimeAsc(date)
                .stream().map(attendanceMapper::toResponse).toList();
    }

    public List<ServiceEventResponse> getServicesByDateRange(LocalDate from, LocalDate to) {
        return serviceEventRepository.findByDateRange(from, to)
                .stream().map(attendanceMapper::toResponse).toList();
    }

    // ── QR Code ────────────────────────────────────────────────────────────────

    @Transactional
    public QrCodeResponse generateQrCode(UUID serviceEventId) {
        ServiceEvent event  = findEventOrThrow(serviceEventId);
        String       token  = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(qrExpiryMinutes);

        event.setQrToken(token);
        event.setQrExpiresAt(expiry);
        serviceEventRepository.save(event);

        String base64Image = qrCodeService.generateQrCodeBase64(token);
        log.info("QR code generated for event: {} — expires at {}", event.getTitle(), expiry);

        return QrCodeResponse.builder()
                .qrToken(token)
                .qrImageBase64(base64Image)
                .expiresAt(expiry)
                .serviceEventTitle(event.getTitle())
                .serviceEventId(serviceEventId.toString())
                .build();
    }

    public byte[] getQrCodeImage(UUID serviceEventId) {
        ServiceEvent event = findEventOrThrow(serviceEventId);
        if (event.getQrToken() == null) {
            throw new ResourceNotFoundException(
                    "No QR code generated yet for this event. Call POST /qr first.");
        }
        return qrCodeService.generateQrCodeBytes(event.getQrToken());
    }

    // ── Check-in ───────────────────────────────────────────────────────────────

    @Transactional
    public AttendanceRecordResponse checkInManual(CheckInRequest request) {
        ServiceEvent event = findEventOrThrow(request.getServiceEventId());
        validateNotAlreadyCheckedIn(event.getId(), request.getMembershipNumber());

        AttendanceRecord record = AttendanceRecord.builder()
                .serviceEvent(event)
                .membershipNumber(request.getMembershipNumber().toUpperCase())
                .memberName(request.getMemberName())
                .checkinMethod(CheckInMethod.MANUAL)
                .checkedInAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        AttendanceRecord saved = attendanceRepository.save(record);
        log.info("Manual check-in: {} for event {}",
                request.getMembershipNumber(), event.getTitle());
        return attendanceMapper.toResponse(saved);
    }

    @Transactional
    public AttendanceRecordResponse checkInByQr(QrCheckInRequest request) {
        ServiceEvent event = serviceEventRepository.findByQrToken(request.getQrToken())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid QR code. Please scan a valid check-in QR code."));

        if (!event.isQrValid()) {
            throw new QrTokenExpiredException(
                    "This QR code has expired. Please ask a leader to generate a new one.");
        }

        validateNotAlreadyCheckedIn(event.getId(), request.getMembershipNumber());

        AttendanceRecord record = AttendanceRecord.builder()
                .serviceEvent(event)
                .membershipNumber(request.getMembershipNumber().toUpperCase())
                .memberName(request.getMemberName())
                .checkinMethod(CheckInMethod.QR_CODE)
                .checkedInAt(LocalDateTime.now())
                .build();

        AttendanceRecord saved = attendanceRepository.save(record);
        log.info("QR check-in: {} for event {}",
                request.getMembershipNumber(), event.getTitle());
        return attendanceMapper.toResponse(saved);
    }

    // ── Attendance queries ─────────────────────────────────────────────────────

    public List<AttendanceRecordResponse> getAttendanceByEvent(UUID serviceEventId) {
        findEventOrThrow(serviceEventId);
        return attendanceRepository.findByServiceEventIdOrderByCheckedInAtAsc(serviceEventId)
                .stream().map(attendanceMapper::toResponse).toList();
    }

    public PagedResponse<AttendanceRecordResponse> getMemberHistory(
            String membershipNumber, Pageable pageable) {
        return PagedResponse.of(
                attendanceRepository
                        .findByMembershipNumberOrderByCheckedInAtDesc(
                                membershipNumber.toUpperCase(), pageable)
                        .map(attendanceMapper::toResponse));
    }

    public long getMemberAttendanceCount(
            String membershipNumber, LocalDate from, LocalDate to) {
        return attendanceRepository.countMemberAttendanceInRange(
                membershipNumber.toUpperCase(), from, to);
    }

    // ── Statistics ─────────────────────────────────────────────────────────────

    public AttendanceStatsResponse getStats(LocalDate from, LocalDate to) {
        List<ServiceEvent> events = serviceEventRepository.findByDateRange(from, to);

        long totalCheckIns = events.stream()
                .mapToLong(e -> attendanceRepository.countByServiceEventId(e.getId()))
                .sum();

        int highest = events.stream()
                .mapToInt(e -> (int) attendanceRepository.countByServiceEventId(e.getId()))
                .max().orElse(0);

        int lowest = events.stream()
                .mapToInt(e -> (int) attendanceRepository.countByServiceEventId(e.getId()))
                .min().orElse(0);

        double average = events.isEmpty() ? 0 : (double) totalCheckIns / events.size();

        List<Object[]> topRows = attendanceRepository.findTopAttendees(
                from, to, PageRequest.of(0, 10));
        List<TopAttendeeResponse> topAttendees = topRows.stream()
                .map(row -> new TopAttendeeResponse(
                        (String) row[0],
                        (String) row[1],
                        (Long)   row[2]))
                .collect(Collectors.toList());

        return AttendanceStatsResponse.builder()
                .from(from).to(to)
                .totalServices(events.size())
                .totalCheckIns(totalCheckIns)
                .averagePerService(average)
                .highestAttendance(highest)
                .lowestAttendance(lowest)
                .topAttendees(topAttendees)
                .build();
    }

    // ── Location availability guard ────────────────────────────────────────────

    /**
     * Checks that no existing event occupies the requested location
     * during the proposed time window on the same date.
     *
     * @param excludeId  pass the event's own UUID when updating so the
     *                   event does not conflict with itself; null for new events.
     */
    private void assertLocationAvailable(
            String    location,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            UUID      excludeId) {

        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time (" + endTime.format(TIME_FMT) + ") must be after " +
                            "start time (" + startTime.format(TIME_FMT) + ").");
        }

        List<ServiceEvent> conflicts = serviceEventRepository.findOverlappingEvents(
                location, date, startTime, endTime, excludeId);

        if (!conflicts.isEmpty()) {
            ServiceEvent conflict = conflicts.get(0);
            throw new LocationConflictException(
                    "Location '" + location + "' is not available on " + date +
                            " from " + startTime.format(TIME_FMT) +
                            " to "   + endTime.format(TIME_FMT)   + ". " +
                            "It is already booked for '" + conflict.getTitle() + "' " +
                            "(" + conflict.getStartTime().format(TIME_FMT) +
                            " – " + conflict.getEndTime().format(TIME_FMT) + ")." +
                            (conflicts.size() > 1
                                    ? " (" + (conflicts.size() - 1) + " other conflict(s) also exist.)"
                                    : ""));
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private ServiceEvent findEventOrThrow(UUID id) {
        return serviceEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service event not found with id: " + id));
    }

    private void validateNotAlreadyCheckedIn(UUID serviceEventId, String membershipNumber) {
        if (attendanceRepository.existsByServiceEventIdAndMembershipNumber(
                serviceEventId, membershipNumber.toUpperCase())) {
            throw new DuplicateCheckInException(
                    "Member " + membershipNumber +
                            " is already checked in to this service.");
        }
    }
}