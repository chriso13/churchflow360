package com.churchflow360.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${churchflow360.jwt.secret:360ChurchFlowSecretKey2026SuperSecureMinimum256BitsLong!!}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── Mode 1: Request came through the API Gateway ──────────────────────
        String xAuthUserId = request.getHeader("X-Auth-User-Id");
        if (xAuthUserId != null && !xAuthUserId.isBlank()) {
            String role     = request.getHeader("X-Auth-Role");
            String username = request.getHeader("X-Auth-Username");
            String authority = toAuthority(role != null ? role : "MEMBER");

            setAuthentication(username != null ? username : xAuthUserId, authority);
            log.debug("Gateway auth: userId={} role={}", xAuthUserId, authority);
            filterChain.doFilter(request, response);
            return;
        }

        // ── Mode 2: Direct call with Bearer token ─────────────────────────────
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String role     = claims.get("role", String.class);
                String username = claims.getSubject();
                setAuthentication(username, toAuthority(role != null ? role : "MEMBER"));
                log.debug("Direct JWT auth: username={}", username);
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String principal, String authority) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal, null,
                        List.of(new SimpleGrantedAuthority(authority))
                );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String toAuthority(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
