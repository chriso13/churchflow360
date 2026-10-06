package com.churchflow360.memberservice.security;

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
 * Spring Security configuration for member-service.
 *
 * Authentication is handled by the API Gateway which validates the JWT
 * and forwards caller identity as X-Auth-* headers.
 *
 * Role matrix:
 * <pre>
 * Endpoint                          MEMBER      LEADER     PASTOR     ADMIN
 * POST   /api/v1/members            ✗           ✗          ✓          ✓
 * GET    /api/v1/members            ✗           ✓          ✓          ✓
 * GET    /api/v1/members/{id}       own only    ✓          ✓          ✓
 * GET    /api/v1/members/number/{n} own only    ✓          ✓          ✓  (+ internal permit)
 * PUT    /api/v1/members/{id}       own only    ✓          ✓          ✓
 * PATCH  /api/v1/members/{id}/status ✗          ✓          ✓          ✓
 * GET    /api/v1/members/stats       ✗          ✓          ✓          ✓
 * DELETE /api/v1/members/{id}        ✗          ✗          ✗          ✓
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class MemberSecurityConfig {

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

                        // ── Actuator & Swagger ───────────────────────────────────────
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ── Internal service-to-service ──────────────────────────────
                        // Permits membership number lookup without a JWT.
                        // Used by auth-service (Feign) to verify that a membership number
                        // exists in member-service during user registration.
                        // Read-only — does not expose sensitive member data.
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/number/**").permitAll()

                        // ── Create member — PASTOR and ADMIN only ────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/members")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Delete member — ADMIN only ───────────────────────────────
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/members/**")
                        .hasRole("ADMIN")

                        // ── Change status — LEADER and above ────────────────────────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/members/*/status")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Stats — LEADER and above ─────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/stats")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── List all members — LEADER and above ──────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/members")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── All other member endpoints — any authenticated user ───────
                        // Fine-grained owner checks (MEMBER can only see own record)
                        // are enforced in the controller via @PreAuthorize
                        .requestMatchers("/api/v1/members/**").authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}