package com.churchflow360.authservice.service;

import com.churchflow360.authservice.client.MemberClient;
import com.churchflow360.authservice.config.JwtProperties;
import com.churchflow360.authservice.dto.request.*;
import com.churchflow360.authservice.dto.response.AuthResponse;
import com.churchflow360.authservice.dto.response.UserResponse;
import com.churchflow360.authservice.exception.*;
import com.churchflow360.authservice.model.*;
import com.churchflow360.authservice.repository.*;
import com.churchflow360.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS    = 5;
    private static final int LOCK_DURATION_MINUTES  = 30;

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil                jwtUtil;
    private final JwtProperties          jwtProperties;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final MemberClient           memberClient;        // ← new dependency

    // ── Login ──────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (AccountStatus.SUSPENDED.equals(user.getStatus())) {
            throw new AuthException("Account is suspended. Contact the church administrator.");
        }
        if (AccountStatus.INACTIVE.equals(user.getStatus())) {
            throw new AuthException("Account is inactive. Contact the church administrator.");
        }

        if (!user.isAccountNonLocked()) {
            throw new AuthException(
                    "Account is temporarily locked due to too many failed attempts. " +
                            "Try again after " + user.getLockedUntil().toString());
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            handleFailedLogin(user);
            throw new AuthException("Invalid credentials");
        }

        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        if (AccountStatus.PENDING_VERIFICATION.equals(user.getStatus())) {
            user.setStatus(AccountStatus.ACTIVE);
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        userRepository.save(user);

        return buildAuthResponse(user, request.getDeviceInfo(), ipAddress);
    }

    // ── Register ───────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse register(RegisterRequest request, UUID createdBy) {

        // ── Uniqueness checks ──────────────────────────────────────────────────
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException(
                    "Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "Email '" + request.getEmail() + "' is already registered.");
        }
        if (request.getMembershipNumber() != null
                && userRepository.existsByMembershipNumber(request.getMembershipNumber())) {
            throw new ConflictException(
                    "Membership number '" + request.getMembershipNumber() +
                            "' already has an account.");
        }

        // ── Member-service verification ────────────────────────────────────────
        // If a membership number is provided it MUST exist in member-service.
        // This prevents creating auth accounts linked to non-existent members.
        // Internal staff accounts (no membershipNumber) skip this check.
        if (request.getMembershipNumber() != null
                && !request.getMembershipNumber().isBlank()) {
            memberClient.verifyMemberExists(request.getMembershipNumber());
        }

        // ── Create the user ────────────────────────────────────────────────────
        Role role = (request.getRole() != null) ? request.getRole() : Role.MEMBER;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .membershipNumber(request.getMembershipNumber() != null
                        ? request.getMembershipNumber().toUpperCase()
                        : null)
                .role(role)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .mustChangePassword(false)
                .createdBy(createdBy)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} [{}] membershipNumber={}",
                saved.getUsername(), saved.getRole(), saved.getMembershipNumber());
        return toUserResponse(saved);
    }

    // ── Refresh Token ──────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        String tokenHash = jwtUtil.hashRefreshToken(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Invalid or expired refresh token"));

        if (!stored.isValid()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId(), LocalDateTime.now());
            throw new AuthException(
                    "Refresh token has been revoked or expired. Please log in again.");
        }

        stored.revoke();
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        return buildAuthResponse(user, stored.getDeviceInfo(), ipAddress);
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        String hash = jwtUtil.hashRefreshToken(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
            t.revoke();
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("All refresh tokens revoked for user {}", userId);
    }

    // ── Change Password ────────────────────────────────────────────────────────

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException(
                    "New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("Password changed for user {}", userId);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, String deviceInfo, String ipAddress) {
        String    accessToken = jwtUtil.generateAccessToken(user);
        String    rawRefresh  = jwtUtil.generateRefreshToken();
        String    refreshHash = jwtUtil.hashRefreshToken(rawRefresh);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiryMs() / 1000))
                .build();
        refreshTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiryMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .membershipNumber(user.getMembershipNumber())
                        .mustChangePassword(user.getMustChangePassword())
                        .build())
                .build();
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount(LOCK_DURATION_MINUTES);
            log.warn("Account locked for user {} after {} failed attempts",
                    user.getUsername(), MAX_FAILED_ATTEMPTS);
        }
        userRepository.save(user);
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .membershipNumber(user.getMembershipNumber())
                .emailVerified(user.getEmailVerified())
                .mustChangePassword(user.getMustChangePassword())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}