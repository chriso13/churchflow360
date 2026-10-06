package com.churchflow360.authservice.util;

import com.churchflow360.authservice.config.JwtProperties;
import com.churchflow360.authservice.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT utility — generates, validates, and parses JSON Web Tokens.
 *
 * <p>Token structure (claims):
 * <ul>
 *   <li>{@code sub}      — username</li>
 *   <li>{@code userId}   — user UUID</li>
 *   <li>{@code email}    — email address</li>
 *   <li>{@code role}     — MEMBER | LEADER | PASTOR | ADMIN</li>
 *   <li>{@code membershipNumber} — linked church membership (may be null)</li>
 *   <li>{@code fullName} — display name</li>
 *   <li>{@code iss}      — issuer (360-church-flow)</li>
 *   <li>{@code iat}      — issued at</li>
 *   <li>{@code exp}      — expiry</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final JwtProperties jwtProperties;

    // ── Token generation ───────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        return buildToken(user, jwtProperties.getAccessTokenExpiryMs());
    }

    public String generateRefreshToken() {
        // Refresh token is a random opaque UUID — not a JWT
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    private String buildToken(User user, long expiryMs) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claims(Map.of(
                        "userId",           user.getId().toString(),
                        "email",            user.getEmail(),
                        "role",             user.getRole().name(),
                        "fullName",         user.getFullName(),
                        "membershipNumber", user.getMembershipNumber() != null
                                ? user.getMembershipNumber() : ""
                ))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT unsupported: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformed: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature invalid: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims empty: {}", ex.getMessage());
        }
        return false;
    }

    // ── Claims extraction ──────────────────────────────────────────────────────

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    // ── Refresh token hashing ──────────────────────────────────────────────────

    /**
     * SHA-256 hash of the raw refresh token for secure storage.
     * Never store the raw refresh token in the database.
     */
    public String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to hash refresh token", ex);
        }
    }

    // ── Key ───────────────────────────────────────────────────────────────────

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret()
                .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}