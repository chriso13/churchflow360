package com.churchflow360.prayerrequest.service;

import com.churchflow360.prayerrequest.client.AuthClient;
import com.churchflow360.prayerrequest.client.MemberClient;
import com.churchflow360.prayerrequest.dto.request.*;
import com.churchflow360.prayerrequest.dto.response.*;
import com.churchflow360.prayerrequest.exception.NotAPastorException;
import com.churchflow360.prayerrequest.exception.ResourceNotFoundException;
import com.churchflow360.prayerrequest.model.*;
import com.churchflow360.prayerrequest.repository.PrayerRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
@Transactional(readOnly = true)
public class PrayerRequestService {

    private final PrayerRequestRepository repository;
    private final MemberClient memberClient;
    private final AuthClient authClient;

    @Transactional
    public PrayerRequestResponse create(CreatePrayerRequest req) {
        // Verify requester member exists (if not anonymous)
        if (!Boolean.TRUE.equals(req.getIsAnonymous())
                && req.getRequesterMembershipNumber() != null
                && !req.getRequesterMembershipNumber().isBlank()) {
            memberClient.verifyMemberExists(req.getRequesterMembershipNumber());
        }

        // Verify assigned pastor exists AND has PASTOR role
        if (req.getAssignedPastorNumber() != null
                && !req.getAssignedPastorNumber().isBlank()) {
            verifyIsPastor(req.getAssignedPastorNumber());
        }

        PrayerRequest prayer = PrayerRequest.builder()
                .requesterMembershipNumber(req.getRequesterMembershipNumber())
                .requesterName(Boolean.TRUE.equals(req.getIsAnonymous()) ? "Anonymous" : req.getRequesterName())
                .title(req.getTitle())
                .requestText(req.getRequestText())
                .category(req.getCategory() != null ? req.getCategory() : RequestCategory.OTHER)
                .privacyLevel(req.getPrivacyLevel() != null ? req.getPrivacyLevel() : PrivacyLevel.PUBLIC)
                .urgency(req.getUrgency() != null ? req.getUrgency() : UrgencyLevel.NORMAL)
                .isAnonymous(Boolean.TRUE.equals(req.getIsAnonymous()))
                .status(RequestStatus.ACTIVE)
                .prayerCount(0)
                .assignedPastorNumber(req.getAssignedPastorNumber())
                .expiresAt(req.getExpiryDays() != null
                        ? LocalDateTime.now().plusDays(req.getExpiryDays()) : null)
                .build();

        PrayerRequest saved = repository.save(prayer);
        log.info("Prayer request created: [{}] {} — privacy={}", saved.getCategory(),
                saved.getTitle(), saved.getPrivacyLevel());
        return toResponse(saved);
    }

    public Page<PrayerRequestResponse> getAll(String role, Pageable pageable) {
        // Determine visible privacy levels based on caller's role
        List<PrivacyLevel> visible = visibleLevels(role);
        return repository.findByPrivacyLevelInOrderByCreatedAtDesc(visible, pageable)
                .map(this::toResponse);
    }

    public Page<PrayerRequestResponse> getActive(String role, Pageable pageable) {
        List<PrivacyLevel> visible = visibleLevels(role);
        return repository.findByStatusAndPrivacyLevelInOrderByCreatedAtDesc(
                RequestStatus.ACTIVE, visible, pageable).map(this::toResponse);
    }

    public Page<PrayerRequestResponse> getByMember(String membershipNumber, Pageable pageable) {
        return repository.findByRequesterMembershipNumberOrderByCreatedAtDesc(
                membershipNumber.toUpperCase(), pageable).map(this::toResponse);
    }

    public Page<PrayerRequestResponse> getByCategory(RequestCategory category, String role, Pageable pageable) {
        return repository.findByCategoryOrderByCreatedAtDesc(category, pageable)
                .map(this::toResponse);
    }

    public List<PrayerRequestResponse> getUrgent() {
        return repository.findUrgentActive().stream().map(this::toResponse).toList();
    }

    public PrayerRequestResponse getOne(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public PrayerRequestResponse incrementPrayerCount(UUID id) {
        PrayerRequest prayer = findOrThrow(id);
        prayer.setPrayerCount(prayer.getPrayerCount() + 1);
        return toResponse(repository.save(prayer));
    }

    @Transactional
    public PrayerRequestResponse markAnswered(UUID id, AnswerPrayerRequest req) {
        PrayerRequest prayer = findOrThrow(id);
        prayer.setStatus(RequestStatus.ANSWERED);
        prayer.setAnsweredTestimony(req.getTestimony());
        prayer.setAnsweredAt(LocalDateTime.now());
        log.info("Prayer request answered: {}", id);
        return toResponse(repository.save(prayer));
    }

    @Transactional
    public PrayerRequestResponse updateStatus(UUID id, RequestStatus status) {
        PrayerRequest prayer = findOrThrow(id);
        prayer.setStatus(status);
        return toResponse(repository.save(prayer));
    }

    @Transactional
    public PrayerRequestResponse assignPastor(UUID id, String pastorNumber) {
        PrayerRequest prayer = findOrThrow(id);

        // Verify the assigned person exists AND has PASTOR role
        if (pastorNumber != null && !pastorNumber.isBlank()) {
            verifyIsPastor(pastorNumber);
        }

        prayer.setAssignedPastorNumber(pastorNumber);
        return toResponse(repository.save(prayer));
    }

    @Transactional
    public void delete(UUID id) { repository.delete(findOrThrow(id)); }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", repository.count());
        stats.put("active", repository.countByStatus(RequestStatus.ACTIVE));
        stats.put("answered", repository.countByStatus(RequestStatus.ANSWERED));
        stats.put("urgent", repository.findUrgentActive().size());
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (RequestCategory c : RequestCategory.values()) {
            long count = repository.countByCategory(c);
            if (count > 0) byCategory.put(c.name(), count);
        }
        stats.put("byCategory", byCategory);
        return stats;
    }

    // Determine which privacy levels the caller can see
    private List<PrivacyLevel> visibleLevels(String role) {
        if (role == null) return List.of(PrivacyLevel.PUBLIC);
        return switch (role.toUpperCase()) {
            case "ADMIN", "PASTOR" -> List.of(PrivacyLevel.PUBLIC, PrivacyLevel.LEADERS_ONLY,
                    PrivacyLevel.PASTOR_ONLY);
            case "LEADER"          -> List.of(PrivacyLevel.PUBLIC, PrivacyLevel.LEADERS_ONLY);
            default                -> List.of(PrivacyLevel.PUBLIC);
        };
    }

    // ── Pastor verification ───────────────────────────────────────────────────

    private void verifyIsPastor(String membershipNumber) {
        // 1. Verify the member exists
        memberClient.verifyMemberExists(membershipNumber);

        // 2. Verify the member has PASTOR role via auth-service
        try {
            var response = authClient.getPastorUsers(0, 200);
            if (response != null && response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof java.util.Map<?,?> dataMap) {
                    Object content = dataMap.get("content");
                    if (content instanceof java.util.List<?> users) {
                        boolean isPastor = users.stream()
                                .filter(u -> u instanceof java.util.Map)
                                .map(u -> (java.util.Map<?,?>) u)
                                .anyMatch(u -> membershipNumber.equalsIgnoreCase(
                                        String.valueOf(u.get("membershipNumber"))));
                        if (!isPastor) {
                            throw new NotAPastorException(
                                    "Member " + membershipNumber + " is not a Pastor. " +
                                    "Only members with PASTOR role can be assigned to prayer requests.");
                        }
                        return;
                    }
                }
            }
        } catch (NotAPastorException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not verify PASTOR role for {}: {}", membershipNumber, e.getMessage());
            // If auth-service is unavailable, allow but log the warning
        }
    }

    private PrayerRequest findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prayer request not found: " + id));
    }

    private PrayerRequestResponse toResponse(PrayerRequest p) {
        PrayerRequestResponse r = new PrayerRequestResponse();
        r.setId(p.getId());
        // Mask identity for anonymous requests unless pastor/admin
        r.setRequesterMembershipNumber(Boolean.TRUE.equals(p.getIsAnonymous()) ? null : p.getRequesterMembershipNumber());
        r.setRequesterName(Boolean.TRUE.equals(p.getIsAnonymous()) ? "Anonymous" : p.getRequesterName());
        r.setTitle(p.getTitle());
        r.setRequestText(p.getRequestText());
        r.setCategory(p.getCategory());
        r.setPrivacyLevel(p.getPrivacyLevel());
        r.setStatus(p.getStatus());
        r.setUrgency(p.getUrgency());
        r.setIsAnonymous(p.getIsAnonymous());
        r.setPrayerCount(p.getPrayerCount());
        r.setAnsweredTestimony(p.getAnsweredTestimony());
        r.setAnsweredAt(p.getAnsweredAt());
        r.setAssignedPastorNumber(p.getAssignedPastorNumber());
        r.setExpiresAt(p.getExpiresAt());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
