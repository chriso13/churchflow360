package com.churchflow360.memberservice.controller;

import com.churchflow360.memberservice.dto.request.ChangeStatusRequest;
import com.churchflow360.memberservice.dto.request.MemberRequest;
import com.churchflow360.memberservice.dto.request.UpdateMemberRequest;
import com.churchflow360.memberservice.dto.response.ApiResponse;
import com.churchflow360.memberservice.dto.response.MemberResponse;
import com.churchflow360.memberservice.dto.response.MemberStatsResponse;
import com.churchflow360.memberservice.dto.response.PagedResponse;
import com.churchflow360.memberservice.model.MemberStatus;
import com.churchflow360.memberservice.security.GatewayAuthPrincipal;
import com.churchflow360.memberservice.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Members", description = "Member profile management")
public class MemberController {

    private final MemberService memberService;

    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "firstName",        "first_name",
            "lastName",         "last_name",
            "email",            "email",
            "membershipStatus", "membership_status",
            "joinDate",         "join_date",
            "createdAt",        "created_at"
    );

    // ════════════════════════════════════════════════════════════════════════════
    // CREATE — PASTOR and ADMIN only
    // ════════════════════════════════════════════════════════════════════════════

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "Create a new member (PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<MemberResponse>> create(
            @Valid @RequestBody MemberRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {
        MemberResponse created = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member created successfully", created));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LIST — LEADER+ sees all
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(
            summary = "List all members (LEADER / PASTOR / ADMIN only)",
            description = "Members with MEMBER role should use " +
                    "GET /api/v1/members/number/{membershipNumber} to retrieve their own profile."
    )
    public ResponseEntity<ApiResponse<PagedResponse<MemberResponse>>> getAll(
            @Parameter(description = "Filter by status: ACTIVE, INACTIVE, VISITOR, TRANSFERRED, DECEASED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by first name, last name or email")
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size,
            @Parameter(description = "Sort field: firstName, lastName, email, membershipStatus, joinDate, createdAt")
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc")      String sortDir) {

        MemberStatus memberStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                memberStatus = MemberStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "Invalid status: '" + status + "'. " +
                                        "Valid values: ACTIVE, INACTIVE, VISITOR, TRANSFERRED, DECEASED"));
            }
        }

        String columnName = SORT_COLUMN_MAP.getOrDefault(sortBy, "last_name");
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(columnName).descending()
                : Sort.by(columnName).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                ApiResponse.ok(memberService.getAll(memberStatus, search, pageable)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GET BY ID — LEADER+ unrestricted; MEMBER only own record
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a member by ID",
            description = "LEADER/PASTOR/ADMIN can view any member. " +
                    "MEMBER can only view their own record."
    )
    public ResponseEntity<ApiResponse<MemberResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        MemberResponse member = memberService.getById(id);

        // principal is null for internal service-to-service calls — allow through
        // MEMBER role — can only view their own profile
        if (principal != null
                && "MEMBER".equals(principal.getRole())
                && !principal.isOwner(member.getMembershipNumber())) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own profile.");
        }

        return ResponseEntity.ok(ApiResponse.ok(member));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GET BY MEMBERSHIP NUMBER — internal + LEADER+ unrestricted; MEMBER own only
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/number/{membershipNumber}")
    @Operation(
            summary = "Get a member by membership number",
            description = "LEADER/PASTOR/ADMIN can look up any member. " +
                    "MEMBER can only look up their own number. " +
                    "Internal service-to-service calls (no JWT) are also permitted."
    )
    public ResponseEntity<ApiResponse<MemberResponse>> getByMembershipNumber(
            @Parameter(description = "Membership number — format: MEM-MMDD-YYYY-HHMMSS")
            @PathVariable String membershipNumber,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // principal is null for internal service-to-service calls (e.g. auth-service
        // verifying membership during registration) — allow through without ownership check
        if (principal != null
                && "MEMBER".equals(principal.getRole())
                && !principal.isOwner(membershipNumber)) {
            throw new AccessDeniedException(
                    "Access denied — you can only view your own membership profile.");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(memberService.getByMembershipNumber(membershipNumber)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UPDATE — LEADER+ unrestricted; MEMBER only own record
    // ════════════════════════════════════════════════════════════════════════════

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a member's profile",
            description = "LEADER/PASTOR/ADMIN can update any member. " +
                    "MEMBER can only update their own profile."
    )
    public ResponseEntity<ApiResponse<MemberResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMemberRequest request,
            @AuthenticationPrincipal GatewayAuthPrincipal principal) {

        // principal is null for internal service-to-service calls — allow through
        // MEMBER role — enforce ownership before updating
        if (principal != null && "MEMBER".equals(principal.getRole())) {
            MemberResponse existing = memberService.getById(id);
            if (!principal.isOwner(existing.getMembershipNumber())) {
                throw new AccessDeniedException(
                        "Access denied — you can only update your own profile.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.ok("Member updated successfully",
                        memberService.updateMember(id, request)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CHANGE STATUS — LEADER and above only
    // ════════════════════════════════════════════════════════════════════════════

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Change a member's membership status (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<MemberResponse>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Status changed successfully",
                        memberService.changeStatus(id, request)));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // STATS — LEADER and above only
    // ════════════════════════════════════════════════════════════════════════════

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get membership statistics (LEADER / PASTOR / ADMIN only)")
    public ResponseEntity<ApiResponse<MemberStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getStats()));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DELETE — ADMIN only
    // ════════════════════════════════════════════════════════════════════════════

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a member (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok("Member deleted successfully", null));
    }
}