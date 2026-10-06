package com.churchflow360.smallgroups.service;

import com.churchflow360.smallgroups.client.MemberClient;
import com.churchflow360.smallgroups.dto.request.*;
import com.churchflow360.smallgroups.dto.response.*;
import com.churchflow360.smallgroups.exception.*;
import com.churchflow360.smallgroups.model.*;
import com.churchflow360.smallgroups.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SmallGroupService {

    private final SmallGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final MemberClient memberClient;

    // ── Groups ────────────────────────────────────────────────────────────────

    @Transactional
    public SmallGroupResponse createGroup(CreateGroupRequest request) {
        // Verify leader exists in member-service before saving
        memberClient.verifyMemberExists(request.getLeaderMembershipNumber());



        SmallGroup group = SmallGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .groupType(request.getGroupType())
                .status(GroupStatus.ACTIVE)
                .meetingFrequency(request.getMeetingFrequency())
                .meetingDay(request.getMeetingDay())
                .meetingTime(request.getMeetingTime())
                .meetingLocation(request.getMeetingLocation())
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 20)
                .leaderMembershipNumber(request.getLeaderMembershipNumber().toUpperCase())
                .leaderName(request.getLeaderName())
                .targetArea(request.getTargetArea())
                .notes(request.getNotes())
                .build();

        SmallGroup saved = groupRepository.save(group);

        // Auto-add leader as first member
        GroupMembership leaderMembership = GroupMembership.builder()
                .group(saved)
                .membershipNumber(request.getLeaderMembershipNumber().toUpperCase())
                .memberName(request.getLeaderName())
                .role(MemberRole.LEADER)
                .isActive(true)
                .build();
        membershipRepository.save(leaderMembership);

        log.info("Small group created: {} [{}]", saved.getName(), saved.getGroupType());
        return toResponse(saved);
    }

    public Page<SmallGroupResponse> getAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<SmallGroupResponse> getByStatus(GroupStatus status, Pageable pageable) {
        return groupRepository.findByStatusOrderByNameAsc(status, pageable).map(this::toResponse);
    }

    public Page<SmallGroupResponse> getByType(GroupType type, Pageable pageable) {
        return groupRepository.findByGroupTypeOrderByNameAsc(type, pageable).map(this::toResponse);
    }

    public Page<SmallGroupResponse> search(String query, Pageable pageable) {
        return groupRepository.search(query, pageable).map(this::toResponse);
    }

    public SmallGroupResponse getGroup(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public List<SmallGroupResponse> getGroupsByLeader(String membershipNumber) {
        return groupRepository.findByLeaderMembershipNumberOrderByNameAsc(membershipNumber.toUpperCase())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public SmallGroupResponse updateGroup(UUID id, CreateGroupRequest request) {
        SmallGroup group = findOrThrow(id);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setGroupType(request.getGroupType());
        group.setMeetingFrequency(request.getMeetingFrequency());
        group.setMeetingDay(request.getMeetingDay());
        group.setMeetingTime(request.getMeetingTime());
        group.setMeetingLocation(request.getMeetingLocation());
        if (request.getMaxMembers() != null) group.setMaxMembers(request.getMaxMembers());
        if (request.getLeaderMembershipNumber() != null)
            group.setLeaderMembershipNumber(request.getLeaderMembershipNumber().toUpperCase());
        if (request.getLeaderName() != null) group.setLeaderName(request.getLeaderName());
        if (request.getTargetArea() != null) group.setTargetArea(request.getTargetArea());
        if (request.getNotes() != null) group.setNotes(request.getNotes());
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public SmallGroupResponse updateStatus(UUID id, GroupStatus status) {
        SmallGroup group = findOrThrow(id);
        group.setStatus(status);
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(UUID id) {
        groupRepository.delete(findOrThrow(id));
    }

    // ── Memberships ───────────────────────────────────────────────────────────

    @Transactional
    public GroupMembershipResponse addMember(UUID groupId, AddMemberRequest request) {
        SmallGroup group = findOrThrow(groupId);

        // Verify member exists in member-service before adding
        memberClient.verifyMemberExists(request.getMembershipNumber());

        if (membershipRepository.existsByGroupIdAndMembershipNumberAndIsActiveTrue(
                groupId, request.getMembershipNumber().toUpperCase())) {
            throw new DuplicateMemberException("Member is already in this group.");
        }

        long currentCount = membershipRepository.countByGroupIdAndIsActiveTrue(groupId);
        if (currentCount >= group.getMaxMembers()) {
            group.setStatus(GroupStatus.FULL);
            groupRepository.save(group);
            throw new GroupFullException("Group has reached its maximum capacity of "
                    + group.getMaxMembers() + " members.");
        }

        GroupMembership membership = GroupMembership.builder()
                .group(group)
                .membershipNumber(request.getMembershipNumber().toUpperCase())
                .memberName(request.getMemberName())
                .role(request.getRole() != null ? request.getRole() : MemberRole.MEMBER)
                .isActive(true)
                .build();

        GroupMembership saved = membershipRepository.save(membership);
        log.info("Member {} added to group {}", request.getMembershipNumber(), group.getName());
        return toMemberResponse(saved);
    }

    public List<GroupMembershipResponse> getGroupMembers(UUID groupId) {
        findOrThrow(groupId);
        return membershipRepository.findByGroupIdAndIsActiveTrueOrderByRoleAsc(groupId)
                .stream().map(this::toMemberResponse).toList();
    }

    public List<GroupMembershipResponse> getMemberGroups(String membershipNumber) {
        return membershipRepository.findByMembershipNumberAndIsActiveTrue(membershipNumber.toUpperCase())
                .stream().map(this::toMemberResponse).toList();
    }

    @Transactional
    public void removeMember(UUID groupId, String membershipNumber) {
        GroupMembership membership = membershipRepository
                .findByGroupIdAndMembershipNumber(groupId, membershipNumber.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this group."));
        membership.setIsActive(false);
        membershipRepository.save(membership);

        // If group was FULL, set back to ACTIVE
        SmallGroup group = findOrThrow(groupId);
        if (group.getStatus() == GroupStatus.FULL) {
            group.setStatus(GroupStatus.ACTIVE);
            groupRepository.save(group);
        }
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalGroups", groupRepository.count());
        stats.put("activeGroups", groupRepository.countByStatus(GroupStatus.ACTIVE));
        stats.put("inactiveGroups", groupRepository.countByStatus(GroupStatus.INACTIVE));
        stats.put("fullGroups", groupRepository.countByStatus(GroupStatus.FULL));
        Map<String, Long> byType = new LinkedHashMap<>();
        for (GroupType type : GroupType.values()) {
            long count = groupRepository.countByGroupType(type);
            if (count > 0) byType.put(type.name(), count);
        }
        stats.put("byType", byType);
        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SmallGroup findOrThrow(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }

    private SmallGroupResponse toResponse(SmallGroup g) {
        SmallGroupResponse r = new SmallGroupResponse();
        r.setId(g.getId());
        r.setName(g.getName());
        r.setDescription(g.getDescription());
        r.setGroupType(g.getGroupType());
        r.setStatus(g.getStatus());
        r.setMeetingFrequency(g.getMeetingFrequency());
        r.setMeetingDay(g.getMeetingDay());
        r.setMeetingTime(g.getMeetingTime());
        r.setMeetingLocation(g.getMeetingLocation());
        r.setMaxMembers(g.getMaxMembers());
        r.setCurrentMemberCount(membershipRepository.countByGroupIdAndIsActiveTrue(g.getId()));
        r.setLeaderMembershipNumber(g.getLeaderMembershipNumber());
        r.setLeaderName(g.getLeaderName());
        r.setTargetArea(g.getTargetArea());
        r.setNotes(g.getNotes());
        r.setCreatedAt(g.getCreatedAt());
        r.setUpdatedAt(g.getUpdatedAt());
        return r;
    }

    private GroupMembershipResponse toMemberResponse(GroupMembership m) {
        GroupMembershipResponse r = new GroupMembershipResponse();
        r.setId(m.getId());
        r.setGroupId(m.getGroup().getId());
        r.setGroupName(m.getGroup().getName());
        r.setMembershipNumber(m.getMembershipNumber());
        r.setMemberName(m.getMemberName());
        r.setRole(m.getRole());
        r.setJoinedDate(m.getJoinedDate());
        r.setIsActive(m.getIsActive());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
