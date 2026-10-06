package com.churchflow360.eventservice.security;

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
 * Spring Security configuration for event-service.
 *
 * Authentication is handled by the API Gateway which validates the JWT
 * and forwards caller identity as X-Auth-* headers.
 *
 * Role matrix:
 * <pre>
 * Endpoint                               MEMBER      LEADER     PASTOR     ADMIN
 * POST   /events                         ✗           ✓          ✓          ✓
 * GET    /events/**  (published)         ✓           ✓          ✓          ✓
 * GET    /events/upcoming                ✓           ✓          ✓          ✓
 * GET    /events/search                  ✓           ✓          ✓          ✓
 * PUT    /events/{id}                    ✗           ✓          ✓          ✓
 * PATCH  /events/{id}/publish            ✗           ✓          ✓          ✓
 * PATCH  /events/{id}/cancel             ✗           ✗          ✓          ✓
 * PATCH  /events/{id}/complete           ✗           ✗          ✓          ✓
 * DELETE /events/{id}                    ✗           ✗          ✗          ✓
 * POST   /events/{id}/rsvp               ✓ (own)     ✓          ✓          ✓
 * GET    /events/{id}/rsvps              ✗           ✓          ✓          ✓
 * GET    /events/{id}/rsvps/attending    ✗           ✓          ✓          ✓
 * GET    /events/{id}/rsvps/{memNum}     own only    ✓          ✓          ✓
 * GET    /events/rsvps/member/{memNum}   own only    ✓          ✓          ✓
 * GET    /events/{id}/stats              ✗           ✓          ✓          ✓
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class EventSecurityConfig {

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

                        // ── Public / internal ────────────────────────────────────────
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ── DELETE — ADMIN only ──────────────────────────────────────
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ADMIN")

                        // ── Cancel / complete — PASTOR and above ─────────────────────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/events/*/cancel")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/events/*/complete")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Create, update, publish — LEADER and above ───────────────
                        .requestMatchers(HttpMethod.POST,  "/api/v1/events").hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,   "/api/v1/events/**").hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/events/*/publish")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── RSVP list, attending list, stats — LEADER and above ──────
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/*/rsvps")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/*/rsvps/attending")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/*/stats")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Read events, RSVP submit, own RSVP — any authenticated ───
                        // Fine-grained ownership for RSVP endpoints enforced in controller
                        .requestMatchers(HttpMethod.GET,  "/api/v1/events/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/rsvp").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}