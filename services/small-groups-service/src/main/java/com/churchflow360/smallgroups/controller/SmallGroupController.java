package com.churchflow360.smallgroups.controller;

import com.churchflow360.smallgroups.dto.request.*;
import com.churchflow360.smallgroups.dto.response.*;
import com.churchflow360.smallgroups.model.*;
import com.churchflow360.smallgroups.service.SmallGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Small Groups", description = "Church small groups and cell group management")
public class SmallGroupController {

    private final SmallGroupService groupService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Create a new small group")
    public ResponseEntity<ApiResponse<SmallGroupResponse>> create(
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Group created", groupService.createGroup(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all groups")
    public ResponseEntity<ApiResponse<Page<SmallGroupResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(required = false) GroupType type,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<SmallGroupResponse> result;
        if (search != null && !search.isBlank()) result = groupService.search(search, pageable);
        else if (status != null) result = groupService.getByStatus(status, pageable);
        else if (type != null)   result = groupService.getByType(type, pageable);
        else                     result = groupService.getAllGroups(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<ApiResponse<SmallGroupResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroup(id)));
    }

    @GetMapping("/leader/{membershipNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get groups led by a member")
    public ResponseEntity<ApiResponse<List<SmallGroupResponse>>> getByLeader(
            @PathVariable String membershipNumber) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroupsByLeader(membershipNumber)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Update group details")
    public ResponseEntity<ApiResponse<SmallGroupResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Group updated", groupService.updateGroup(id, request)));
    }

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Update group status")
    public ResponseEntity<ApiResponse<SmallGroupResponse>> updateStatus(
            @PathVariable UUID id, @PathVariable GroupStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", groupService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Delete a group")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.ok("Group deleted", null));
    }

    // ── Membership endpoints ──────────────────────────────────────────────────

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Add a member to a group")
    public ResponseEntity<ApiResponse<GroupMembershipResponse>> addMember(
            @PathVariable UUID id, @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member added", groupService.addMember(id, request)));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active members of a group")
    public ResponseEntity<ApiResponse<List<GroupMembershipResponse>>> getMembers(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroupMembers(id)));
    }

    @DeleteMapping("/{id}/members/{membershipNumber}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Remove a member from a group")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id, @PathVariable String membershipNumber) {
        groupService.removeMember(id, membershipNumber);
        return ResponseEntity.ok(ApiResponse.ok("Member removed", null));
    }

    @GetMapping("/member/{membershipNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all groups a member belongs to")
    public ResponseEntity<ApiResponse<List<GroupMembershipResponse>>> getMemberGroups(
            @PathVariable String membershipNumber) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getMemberGroups(membershipNumber)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Get group statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getStats()));
    }
}
