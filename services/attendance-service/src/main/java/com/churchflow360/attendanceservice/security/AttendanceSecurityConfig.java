package com.churchflow360.attendanceservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security configuration for attendance-service.
 *
 * Authentication is handled by the API Gateway which validates the JWT
 * and forwards caller identity as X-Auth-* headers.
 *
 * Role matrix:
 * <pre>
 * Endpoint                                    MEMBER      LEADER     PASTOR     ADMIN
 * POST   /events                              ✗           ✓          ✓          ✓
 * GET    /events/**                           ✓           ✓          ✓          ✓
 * POST   /events/{id}/qr                      ✗           ✓          ✓          ✓
 * GET    /events/{id}/qr/image               ✓           ✓          ✓          ✓
 * POST   /checkin                             ✗           ✓          ✓          ✓
 * POST   /checkin/qr                          ✓ (own)     ✓          ✓          ✓
 * GET    /events/{id}/attendees               ✗           ✓          ✓          ✓
 * GET    /members/{n}/history                 own only    ✓          ✓          ✓
 * GET    /members/{n}/count                   own only    ✓          ✓          ✓
 * GET    /stats                               ✗           ✓          ✓          ✓
 * GET    /reports/**                          ✗           ✓          ✓          ✓
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class AttendanceSecurityConfig {

    private final GatewayAuthFilter gatewayAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setContentType("application/json");
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write(
                                    "{\"success\":false,\"message\":\"Unauthorized — gateway authentication required\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setContentType("application/json");
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().write(
                                    "{\"success\":false,\"message\":\"Access denied — insufficient role\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth

                        // ── Public / internal ───────────────────────────────────────────
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ── QR self-check-in — any authenticated member ─────────────────
                        // Owner check enforced in controller
                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/checkin/qr").authenticated()

                        // ── QR image download — any authenticated user ──────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/events/*/qr/image").authenticated()

                        // ── View event details — any authenticated user ─────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/events/**").authenticated()

                        // ── Member views own history / count — ownership checked in controller
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/members/**").authenticated()

                        // ── Create service events — LEADER and above ────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/events")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Generate QR — LEADER and above ─────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/events/*/qr")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Manual check-in — LEADER and above ─────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/checkin")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── View attendees list — LEADER and above ──────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/events/*/attendees")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Stats and reports — LEADER and above ────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/stats")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/reports/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Everything else requires authentication ──────────────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}