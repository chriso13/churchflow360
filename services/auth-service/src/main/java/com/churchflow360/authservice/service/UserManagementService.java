package com.churchflow360.authservice.service;

import com.churchflow360.authservice.dto.request.UpdateRoleRequest;
import com.churchflow360.authservice.dto.response.UserResponse;
import com.churchflow360.authservice.dto.response.UserSummaryResponse;
import com.churchflow360.authservice.exception.*;
import com.churchflow360.authservice.model.*;
import com.churchflow360.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserManagementService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService            authService;

    // ── Read ───────────────────────────────────────────────────────────────────

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(authService::toUserResponse);
    }

    public UserResponse getUserById(UUID id) {
        return authService.toUserResponse(findOrThrow(id));
    }

    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRoleOrderByFullNameAsc(role, pageable)
                .map(authService::toUserResponse);
    }

    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        return userRepository.search(query, pageable).map(authService::toUserResponse);
    }

    // ── Role management ────────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateRole(UUID userId, UpdateRoleRequest request, UUID adminId) {
        User user = findOrThrow(userId);

        if (userId.equals(adminId)) {
            throw new ValidationException("You cannot change your own role.");
        }

        Role oldRole = user.getRole();
        user.setRole(request.getRole());
        User saved = userRepository.save(user);

        // Revoke all tokens — role change takes effect on next login
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());

        log.info("Role changed for user {}: {} → {} (by admin {})",
                user.getUsername(), oldRole, request.getRole(), adminId);
        return authService.toUserResponse(saved);
    }

    // ── Status management ──────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateStatus(UUID userId, AccountStatus newStatus, UUID adminId) {
        User user = findOrThrow(userId);

        if (userId.equals(adminId)) {
            throw new ValidationException("You cannot change your own account status.");
        }

        user.setStatus(newStatus);
        User saved = userRepository.save(user);

        // If suspending/deactivating, revoke all tokens immediately
        if (AccountStatus.SUSPENDED.equals(newStatus) || AccountStatus.INACTIVE.equals(newStatus)) {
            refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
            log.info("User {} status set to {} — all tokens revoked", user.getUsername(), newStatus);
        }

        return authService.toUserResponse(saved);
    }

    // ── Account unlock ─────────────────────────────────────────────────────────

    @Transactional
    public UserResponse unlockAccount(UUID userId) {
        User user = findOrThrow(userId);
        user.resetFailedAttempts();
        User saved = userRepository.save(user);
        log.info("Account unlocked for user {}", user.getUsername());
        return authService.toUserResponse(saved);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteUser(UUID userId, UUID adminId) {
        if (userId.equals(adminId)) {
            throw new ValidationException("You cannot delete your own account.");
        }
        User user = findOrThrow(userId);
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        userRepository.delete(user);
        log.info("User {} deleted by admin {}", user.getUsername(), adminId);
    }

    public Page<UserSummaryResponse> getPastors(Pageable pageable) {
        return userRepository.findByRole(Role.PASTOR, pageable)
                .map(u -> {
                    UserSummaryResponse dto = new UserSummaryResponse();
                    dto.setMembershipNumber(u.getMembershipNumber());
                    dto.setFullName(u.getFullName());
                    dto.setMembershipNumber(u.getMembershipNumber());
                    return dto;
                });
    }


    // ── Add this method to UserManagementService.java ─────────────────────────
    // Returns all membership numbers that already have a user account.
    // No sensitive data — just the membership number strings.

    public List<String> getRegisteredMembershipNumbers() {
        return userRepository.findAll().stream()
                .map(User::getMembershipNumber)
                .filter(Objects::nonNull)
                .filter(n -> !n.isBlank())
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }



    // ── Helpers ────────────────────────────────────────────────────────────────

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}