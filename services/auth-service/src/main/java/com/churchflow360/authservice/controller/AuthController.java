package com.churchflow360.authservice.controller;

import com.churchflow360.authservice.dto.request.*;
import com.churchflow360.authservice.dto.response.*;
import com.churchflow360.authservice.model.User;
import com.churchflow360.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, register, token refresh and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with username/email and password",
            description = "Returns access token (24h) and refresh token (7d).")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(
                ApiResponse.ok("Login successful", authService.login(request, ip)));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user account",
            description = "Creates an ACTIVE account. Role defaults to MEMBER.")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created",
                        authService.register(request, null)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
            description = "Rotates the refresh token — old token is invalidated immediately.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(
                ApiResponse.ok("Token refreshed", authService.refreshToken(request, ip)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/logout-all")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout from all devices — revokes all refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal User user) {
        authService.logoutAll(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Logged out from all devices", null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(authService.toUserResponse(user)));
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password",
            description = "Requires current password. Revokes all refresh tokens after success.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed. Please log in again.", null));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}