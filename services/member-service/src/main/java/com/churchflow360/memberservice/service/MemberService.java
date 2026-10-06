package com.churchflow360.memberservice.service;

import com.churchflow360.memberservice.dto.request.ChangeStatusRequest;
import com.churchflow360.memberservice.dto.request.MemberRequest;
import com.churchflow360.memberservice.dto.request.UpdateMemberRequest;
import com.churchflow360.memberservice.dto.response.MemberResponse;
import com.churchflow360.memberservice.dto.response.MemberStatsResponse;
import com.churchflow360.memberservice.dto.response.PagedResponse;
import com.churchflow360.memberservice.exception.DuplicateResourceException;
import com.churchflow360.memberservice.exception.ResourceNotFoundException;
import com.churchflow360.memberservice.mapper.MemberMapper;
import com.churchflow360.memberservice.model.Member;
import com.churchflow360.memberservice.model.MemberStatus;
import com.churchflow360.memberservice.model.MemberStatusHistory;
import com.churchflow360.memberservice.repository.MemberRepository;
import com.churchflow360.memberservice.repository.MemberStatusHistoryRepository;
import com.churchflow360.memberservice.utility.MembershipNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository              memberRepository;
    private final MemberStatusHistoryRepository statusHistoryRepository;
    private final MemberMapper                  memberMapper;
    private final MemberNotificationService     notificationService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        log.info("Creating member: {} {}", request.getFirstName(), request.getLastName());

        if (request.getEmail() != null && memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "A member with email '" + request.getEmail() + "' already exists");
        }

        Member member = memberMapper.toEntity(request);
        member.setMembershipNumber(MembershipNumberGenerator.generate());

        Member saved = memberRepository.save(member);
        log.info("Member created — id: {} membershipNumber: {}",
                saved.getId(), saved.getMembershipNumber());

        // Notify via notification-service (email + SMS if phone available)
        notificationService.sendWelcome(
                saved.getEmail(),
                saved.getPhone(),
                saved.getFirstName() + " " + saved.getLastName(),
                saved.getMembershipNumber());

        return memberMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public MemberResponse getById(UUID id) {
        return memberMapper.toResponse(findOrThrow(id));
    }

    public MemberResponse getByMembershipNumber(String membershipNumber) {
        Member member = memberRepository.findByMembershipNumber(membershipNumber.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found with membership number: " + membershipNumber));
        return memberMapper.toResponse(member);
    }

    public PagedResponse<MemberResponse> getAll(MemberStatus status,
                                                String search,
                                                Pageable pageable) {
        String searchParam = (search != null && search.isBlank()) ? null : search;

        Page<Member> page;
        if (status == null) {
            page = memberRepository.searchWithoutStatus(searchParam, pageable);
        } else {
            page = memberRepository.searchWithStatus(status.name(), searchParam, pageable);
        }

        return PagedResponse.of(page.map(memberMapper::toResponse));
    }

    public MemberStatsResponse getStats() {
        return MemberStatsResponse.builder()
                .total(memberRepository.count())
                .active(memberRepository.countByMembershipStatus(MemberStatus.ACTIVE))
                .inactive(memberRepository.countByMembershipStatus(MemberStatus.INACTIVE))
                .visitors(memberRepository.countByMembershipStatus(MemberStatus.VISITOR))
                .transferred(memberRepository.countByMembershipStatus(MemberStatus.TRANSFERRED))
                .deceased(memberRepository.countByMembershipStatus(MemberStatus.DECEASED))
                .build();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse updateMember(UUID id, UpdateMemberRequest request) {
        log.info("Updating member: {}", id);

        Member member = findOrThrow(id);

        if (request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(member.getEmail())
                && memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already in use");
        }

        memberMapper.updateEntityFromRequest(request, member);
        Member saved = memberRepository.save(member);
        log.info("Member updated — id: {} membershipNumber: {}",
                saved.getId(), saved.getMembershipNumber());

        notificationService.sendProfileUpdated(
                saved.getEmail(),
                saved.getPhone(),
                saved.getFirstName() + " " + saved.getLastName(),
                saved.getMembershipNumber());

        return memberMapper.toResponse(saved);
    }

    // ── Status change ─────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse changeStatus(UUID id, ChangeStatusRequest request) {
        Member member = findOrThrow(id);
        MemberStatus oldStatus = member.getMembershipStatus();

        if (oldStatus == request.getNewStatus()) {
            throw new IllegalArgumentException(
                    "Member is already in status: " + oldStatus);
        }

        member.setMembershipStatus(request.getNewStatus());
        Member saved = memberRepository.save(member);

        statusHistoryRepository.save(MemberStatusHistory.builder()
                .member(saved)
                .oldStatus(oldStatus)
                .newStatus(request.getNewStatus())
                .reason(request.getReason())
                .changedBy(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .build());

        log.info("Member {} status changed: {} → {}", id, oldStatus, request.getNewStatus());

        notificationService.sendStatusChange(
                saved.getEmail(),
                saved.getPhone(),
                saved.getFirstName() + " " + saved.getLastName(),
                saved.getMembershipNumber(),
                request.getNewStatus().name());

        return memberMapper.toResponse(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteMember(UUID id) {
        Member member = findOrThrow(id);

        String email            = member.getEmail();
        String phone            = member.getPhone();
        String fullName         = member.getFirstName() + " " + member.getLastName();
        String membershipNumber = member.getMembershipNumber();

        memberRepository.delete(member);
        log.info("Member deleted — id: {} membershipNumber: {}", id, membershipNumber);

        notificationService.sendAccountDeleted(email, phone, fullName, membershipNumber);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Member findOrThrow(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id.toString()));
    }
}
