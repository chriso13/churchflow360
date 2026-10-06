package com.churchflow360.announcement.service;

import com.churchflow360.announcement.dto.request.CreateAnnouncementRequest;
import com.churchflow360.announcement.dto.response.AnnouncementResponse;
import com.churchflow360.announcement.exception.ResourceNotFoundException;
import com.churchflow360.announcement.model.*;
import com.churchflow360.announcement.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository repository;

    @Transactional
    public AnnouncementResponse create(CreateAnnouncementRequest req) {
        AnnouncementStatus status = req.getScheduledAt() != null
                ? AnnouncementStatus.SCHEDULED
                : AnnouncementStatus.DRAFT;

        Announcement ann = Announcement.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .summary(req.getSummary())
                .category(req.getCategory() != null ? req.getCategory() : AnnouncementCategory.GENERAL)
                .audience(req.getAudience() != null ? req.getAudience() : AudienceType.ALL_MEMBERS)
                .priority(req.getPriority() != null ? req.getPriority() : Priority.NORMAL)
                .status(status)
                .targetGroupId(req.getTargetGroupId())
                .authorMembershipNumber(req.getAuthorMembershipNumber().toUpperCase())
                .authorName(req.getAuthorName())
                .imageUrl(req.getImageUrl())
                .actionUrl(req.getActionUrl())
                .actionLabel(req.getActionLabel())
                .pinToTop(Boolean.TRUE.equals(req.getPinToTop()))
                .expiresAt(req.getExpiresAt())
                .scheduledAt(req.getScheduledAt())
                .viewCount(0)
                .build();

        Announcement saved = repository.save(ann);
        log.info("Announcement created: [{}] {} — status={}", saved.getCategory(),
                saved.getTitle(), saved.getStatus());
        return toResponse(saved);
    }

    public Page<AnnouncementResponse> getActive(String role, Pageable pageable) {
        List<AudienceType> audiences = visibleAudiences(role);
        return repository.findActive(LocalDateTime.now(), audiences, pageable)
                .map(this::toResponse);
    }

    public Page<AnnouncementResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    public Page<AnnouncementResponse> getByStatus(AnnouncementStatus status, Pageable pageable) {
        return repository.findByStatusOrderByPinToTopDescCreatedAtDesc(status, pageable)
                .map(this::toResponse);
    }

    public Page<AnnouncementResponse> getByCategory(AnnouncementCategory category, Pageable pageable) {
        return repository.findByCategoryAndStatusOrderByCreatedAtDesc(
                category, AnnouncementStatus.PUBLISHED, pageable).map(this::toResponse);
    }

    public AnnouncementResponse getOne(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public AnnouncementResponse publish(UUID id) {
        Announcement ann = findOrThrow(id);
        ann.setStatus(AnnouncementStatus.PUBLISHED);
        ann.setPublishedAt(LocalDateTime.now());
        log.info("Announcement published: {}", ann.getTitle());
        return toResponse(repository.save(ann));
    }

    @Transactional
    public AnnouncementResponse archive(UUID id) {
        Announcement ann = findOrThrow(id);
        ann.setStatus(AnnouncementStatus.ARCHIVED);
        return toResponse(repository.save(ann));
    }

    @Transactional
    public AnnouncementResponse update(UUID id, CreateAnnouncementRequest req) {
        Announcement ann = findOrThrow(id);
        ann.setTitle(req.getTitle());
        ann.setContent(req.getContent());
        if (req.getSummary() != null) ann.setSummary(req.getSummary());
        if (req.getCategory() != null) ann.setCategory(req.getCategory());
        if (req.getAudience() != null) ann.setAudience(req.getAudience());
        if (req.getPriority() != null) ann.setPriority(req.getPriority());
        if (req.getImageUrl() != null) ann.setImageUrl(req.getImageUrl());
        if (req.getActionUrl() != null) ann.setActionUrl(req.getActionUrl());
        if (req.getActionLabel() != null) ann.setActionLabel(req.getActionLabel());
        if (req.getPinToTop() != null) ann.setPinToTop(req.getPinToTop());
        if (req.getExpiresAt() != null) ann.setExpiresAt(req.getExpiresAt());
        return toResponse(repository.save(ann));
    }

    @Transactional
    public AnnouncementResponse incrementViews(UUID id) {
        Announcement ann = findOrThrow(id);
        ann.setViewCount(ann.getViewCount() + 1);
        return toResponse(repository.save(ann));
    }

    @Transactional
    public void delete(UUID id) { repository.delete(findOrThrow(id)); }

    // Auto-publish scheduled announcements every minute
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processScheduled() {
        List<Announcement> due = repository.findDueForPublishing(LocalDateTime.now());
        if (!due.isEmpty()) {
            due.forEach(a -> { a.setStatus(AnnouncementStatus.PUBLISHED); a.setPublishedAt(LocalDateTime.now()); });
            repository.saveAll(due);
            log.info("Auto-published {} scheduled announcement(s)", due.size());
        }
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", repository.count());
        stats.put("published", repository.countByStatus(AnnouncementStatus.PUBLISHED));
        stats.put("drafts", repository.countByStatus(AnnouncementStatus.DRAFT));
        stats.put("scheduled", repository.countByStatus(AnnouncementStatus.SCHEDULED));
        stats.put("urgent", repository.findByStatusAndPriorityOrderByCreatedAtDesc(
                AnnouncementStatus.PUBLISHED, Priority.URGENT).size());
        return stats;
    }

    private List<AudienceType> visibleAudiences(String role) {
        if (role == null) return List.of(AudienceType.ALL_MEMBERS);
        return switch (role.toUpperCase()) {
            case "ADMIN", "PASTOR" -> List.of(AudienceType.ALL_MEMBERS, AudienceType.LEADERS_ONLY, AudienceType.PASTORS_ONLY);
            case "LEADER"          -> List.of(AudienceType.ALL_MEMBERS, AudienceType.LEADERS_ONLY);
            default                -> List.of(AudienceType.ALL_MEMBERS);
        };
    }

    private Announcement findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
    }

    private AnnouncementResponse toResponse(Announcement a) {
        AnnouncementResponse r = new AnnouncementResponse();
        r.setId(a.getId()); r.setTitle(a.getTitle()); r.setContent(a.getContent());
        r.setSummary(a.getSummary()); r.setCategory(a.getCategory()); r.setAudience(a.getAudience());
        r.setStatus(a.getStatus()); r.setPriority(a.getPriority()); r.setTargetGroupId(a.getTargetGroupId());
        r.setPublishedAt(a.getPublishedAt()); r.setExpiresAt(a.getExpiresAt()); r.setScheduledAt(a.getScheduledAt());
        r.setAuthorMembershipNumber(a.getAuthorMembershipNumber()); r.setAuthorName(a.getAuthorName());
        r.setImageUrl(a.getImageUrl()); r.setActionUrl(a.getActionUrl()); r.setActionLabel(a.getActionLabel());
        r.setViewCount(a.getViewCount()); r.setPinToTop(a.getPinToTop());
        r.setCreatedAt(a.getCreatedAt()); r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }
}
