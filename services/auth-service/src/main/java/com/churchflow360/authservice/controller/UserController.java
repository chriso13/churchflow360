package com.churchflow360.authservice.controller;

import com.churchflow360.authservice.dto.request.RegisterRequest;
import com.churchflow360.authservice.dto.request.UpdateRoleRequest;
import com.churchflow360.authservice.dto.response.*;
import com.churchflow360.authservice.model.AccountStatus;
import com.churchflow360.authservice.model.Role;
import com.churchflow360.authservice.model.User;
import com.churchflow360.authservice.service.AuthService;
import com.churchflow360.authservice.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "Admin endpoints for managing church user accounts")
public class UserController {

    private final UserManagementService userService;
    private final AuthService           authService;

    // ── List / Search ──────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (ADMIN only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<UserResponse> result = (search != null && !search.isBlank())
                ? userService.searchUsers(search, pageable)
                : userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('PASTOR', 'ADMIN')")
    @Operation(summary = "List users by role (PASTOR or ADMIN)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @Parameter(description = "MEMBER, LEADER, PASTOR, ADMIN")
            @PathVariable Role role,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return ResponseEntity.ok(ApiResponse.ok(userService.getUsersByRole(role, pageable)));
    }

    // ── Single user ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id.toString()")
    @Operation(summary = "Get user by ID — ADMIN can view anyone; user can view themselves")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user account (ADMIN only)",
            description = "Admin can set any role. Use POST /api/v1/auth/register for self-registration.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created",
                        authService.register(request, admin.getId())));
    }

    // ── Role management ────────────────────────────────────────────────────────

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user's role (ADMIN only)",
            description = "Revokes all active sessions — user must log in again to get new role token.")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.ok("Role updated",
                userService.updateRole(id, request, admin.getId())));
    }

    // ── Status management ──────────────────────────────────────────────────────

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (ADMIN only)",
            description = "ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION. " +
                    "Suspending a user revokes all their tokens immediately.")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable UUID id,
            @Parameter(description = "ACTIVE, INACTIVE, SUSPENDED")
            @PathVariable AccountStatus status,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                userService.updateStatus(id, status, admin.getId())));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock a locked account (ADMIN only)",
            description = "Resets failed login attempts and clears the lock expiry.")
    public ResponseEntity<ApiResponse<UserResponse>> unlock(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Account unlocked",
                userService.unlockAccount(id)));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user account (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        userService.deleteUser(id, admin.getId());
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    // ── Pastor directory (any authenticated user) ─────────────────────────────

    @GetMapping("/pastors")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all pastors — any authenticated user")
    public ResponseEntity<ApiResponse<Page<UserSummaryResponse>>> getPastors(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return ResponseEntity.ok(ApiResponse.ok(userService.getPastors(pageable)));
    }

    // ── Registered membership numbers (LEADER+) ───────────────────────────────
    // Returns only membership numbers — no sensitive user data.
    // Used by the Register page to filter out members who already have accounts.

    @GetMapping("/registered-membership-numbers")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTOR', 'ADMIN')")
    @Operation(summary = "Get membership numbers that already have user accounts (LEADER+)",
            description = "Returns a plain list of membership number strings. " +
                    "Used by the registration form to hide already-registered members.")
    public ResponseEntity<ApiResponse<List<String>>> getRegisteredMembershipNumbers() {
        List<String> numbers = userService.getRegisteredMembershipNumbers();
        return ResponseEntity.ok(ApiResponse.ok(numbers));
    }
}