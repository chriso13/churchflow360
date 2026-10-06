package com.churchflow360.pastoralservice.security;

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
 * Spring Security configuration for pastoral-meeting-service.
 *
 * Authentication is handled by the API Gateway which validates the JWT
 * and forwards caller identity as X-Auth-* headers.
 *
 * Role matrix:
 * <pre>
 * Endpoint                                     MEMBER      LEADER     PASTOR     ADMIN
 * POST   /pastoral-meetings                    ✓ (own)     ✓          ✓          ✓
 * GET    /pastoral-meetings/{id}               own only    ✓          ✓          ✓
 * PATCH  /pastoral-meetings/{id}/confirm       ✗           ✗          ✓          ✓
 * PATCH  /pastoral-meetings/{id}/reschedule    ✗           ✗          ✓          ✓
 * PATCH  /pastoral-meetings/{id}/cancel        own only    ✗          ✓          ✓
 * PATCH  /pastoral-meetings/{id}/complete      ✗           ✗          ✓          ✓
 * PATCH  /pastoral-meetings/{id}/no-show       ✗           ✗          ✓          ✓
 * GET    /pastoral-meetings/pastor/{n}         ✗           ✗          own only   ✓
 * GET    /pastoral-meetings/member/{n}         own only    ✓          ✓          ✓
 * GET    /pastoral-meetings/pastor/{n}/schedule ✗          ✗          own only   ✓
 * GET    /pastoral-meetings/pastor/{n}/pending  ✗          ✗          own only   ✓
 * GET    /pastoral-meetings/pastor/{n}/follow-up ✗         ✗          own only   ✓
 * GET    /pastoral-meetings/pastor/{n}/stats   ✗           ✗          own only   ✓
 * POST   /pastoral-meetings/{id}/notes         ✗           ✗          own only   ✓
 * GET    /pastoral-meetings/{id}/notes         ✗           ✗          own only   ✓
 * DELETE /pastoral-meetings/notes/{noteId}     ✗           ✗          own only   ✓
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class PastoralSecurityConfig {

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

                        // ── Confidential notes — PASTOR and ADMIN only ───────────────
                        // Ownership check (assigned pastor only) enforced in controller
                        .requestMatchers(HttpMethod.POST,   "/api/v1/pastoral-meetings/*/notes")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/pastoral-meetings/*/notes")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/pastoral-meetings/notes/**")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Pastor-specific queries — PASTOR and ADMIN only ──────────
                        // Ownership (pastor can only see own schedule) enforced in controller
                        .requestMatchers(HttpMethod.GET, "/api/v1/pastoral-meetings/pastor/**")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Status workflow — PASTOR and ADMIN only ──────────────────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pastoral-meetings/*/confirm")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pastoral-meetings/*/reschedule")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pastoral-meetings/*/complete")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pastoral-meetings/*/no-show")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Cancel — any authenticated (ownership checked in controller)
                        // MEMBER can cancel own meeting; PASTOR/ADMIN can cancel any
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pastoral-meetings/*/cancel")
                        .authenticated()

                        // ── Request meeting — any authenticated (ownership checked in controller)
                        // MEMBER can only request for themselves
                        .requestMatchers(HttpMethod.POST, "/api/v1/pastoral-meetings")
                        .authenticated()

                        // ── Get meeting by ID — any authenticated (ownership checked in controller)
                        .requestMatchers(HttpMethod.GET, "/api/v1/pastoral-meetings/*")
                        .authenticated()

                        // ── Member meeting history — any authenticated (ownership checked in controller)
                        .requestMatchers(HttpMethod.GET, "/api/v1/pastoral-meetings/member/**")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}