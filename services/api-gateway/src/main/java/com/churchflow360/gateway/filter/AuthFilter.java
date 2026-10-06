package com.churchflow360.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Gateway-level JWT authentication filter.
 *
 * <p>Applied to every protected route via {@code - name: AuthFilter} in routes config.
 * Public routes (login, register, refresh) skip this filter entirely.
 *
 * <p>On a valid JWT the filter:
 * <ol>
 *   <li>Validates signature, issuer and expiry</li>
 *   <li>Extracts claims (userId, username, role, email, membershipNumber)</li>
 *   <li>Forwards them as request headers to the downstream service</li>
 * </ol>
 *
 * <p>Downstream services can read these headers to know who the caller is
 * without re-validating the JWT themselves:
 * <ul>
 *   <li>{@code X-Auth-User-Id}</li>
 *   <li>{@code X-Auth-Username}</li>
 *   <li>{@code X-Auth-Role}</li>
 *   <li>{@code X-Auth-Email}</li>
 *   <li>{@code X-Auth-Membership-Number}</li>
 * </ul>
 */
@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${churchflow360.jwt.secret}")
    private String jwtSecret;

    @Value("${churchflow360.jwt.issuer:360-church-flow}")
    private String jwtIssuer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extract Bearer token
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or invalid Authorization header. " +
                        "Expected: Authorization: Bearer <token>");
            }

            String token = authHeader.substring(7);

            // Validate and parse JWT
            Claims claims;
            try {
                claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Verify issuer
                if (!jwtIssuer.equals(claims.getIssuer())) {
                    return unauthorized(exchange, "Invalid token issuer");
                }

            } catch (ExpiredJwtException ex) {
                log.debug("JWT expired for request to {}", request.getPath());
                return unauthorized(exchange, "Token has expired. Please refresh your token.");
            } catch (JwtException ex) {
                log.warn("JWT validation failed for request to {}: {}", request.getPath(), ex.getMessage());
                return unauthorized(exchange, "Invalid token");
            }

            // Forward identity claims as headers to downstream services
            String userId           = claims.get("userId",           String.class);
            String username         = claims.getSubject();
            String role             = claims.get("role",             String.class);
            String email            = claims.get("email",            String.class);
            String membershipNumber = claims.get("membershipNumber", String.class);

            log.debug("Authenticated {} [{}] → {}", username, role, request.getPath());

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Auth-User-Id",           userId           != null ? userId           : "")
                    .header("X-Auth-Username",           username         != null ? username         : "")
                    .header("X-Auth-Role",               role             != null ? role             : "")
                    .header("X-Auth-Email",              email            != null ? email            : "")
                    .header("X-Auth-Membership-Number",  membershipNumber != null ? membershipNumber : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body;
        try {
            body = objectMapper.writeValueAsString(Map.of(
                    "success",   false,
                    "message",   message,
                    "timestamp", LocalDateTime.now().toString()
            ));
        } catch (JsonProcessingException ex) {
            body = "{\"success\":false,\"message\":\"" + message + "\"}";
        }

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public static class Config {
        // No per-route config needed — all routes use the same JWT validation
    }
}