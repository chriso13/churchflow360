package com.churchflow360.financeservice.security;

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
 * Spring Security configuration for finance-service.
 *
 * Authentication is handled by the API Gateway which validates the JWT
 * and forwards caller identity as X-Auth-* headers.
 *
 * Role matrix:
 * <pre>
 * ── TITHES ──────────────────────────────────────────────────────────────────
 * POST   /tithes                              LEADER / PASTOR / ADMIN
 * GET    /tithes/{id}                         MEMBER (own) / LEADER+
 * GET    /tithes/member/{n}                   MEMBER (own) / LEADER+
 * GET    /tithes/member/{n}/total/{year}      MEMBER (own) / LEADER+
 * GET    /tithes (date range)                 LEADER / PASTOR / ADMIN
 *
 * ── DONATIONS ───────────────────────────────────────────────────────────────
 * POST   /donations                           LEADER / PASTOR / ADMIN
 * GET    /donations/{id}                      LEADER / PASTOR / ADMIN
 * GET    /donations/member/{n}                MEMBER (own) / LEADER+
 * GET    /donations/category/{c}              LEADER / PASTOR / ADMIN
 * GET    /donations (date range)              LEADER / PASTOR / ADMIN
 * PATCH  /donations/{id}/receipt              PASTOR / ADMIN
 *
 * ── OFFERINGS ───────────────────────────────────────────────────────────────
 * POST   /offerings                           LEADER / PASTOR / ADMIN
 * GET    /offerings/{id}                      LEADER / PASTOR / ADMIN
 * GET    /offerings/date/{date}               LEADER / PASTOR / ADMIN
 * GET    /offerings (date range)              LEADER / PASTOR / ADMIN
 * POST   /offerings/{id}/items                LEADER / PASTOR / ADMIN
 * PATCH  /offerings/{id}/verify               PASTOR / ADMIN
 *
 * ── FINANCE SUMMARY ─────────────────────────────────────────────────────────
 * GET    /finance/summary                     PASTOR / ADMIN
 *
 * ── EXPENSES ────────────────────────────────────────────────────────────────
 * POST   /expenses                            LEADER / PASTOR / ADMIN
 * GET    /expenses/{id}                       LEADER / PASTOR / ADMIN
 * GET    /expenses/status/{s}                 LEADER / PASTOR / ADMIN
 * GET    /expenses/category/{c}               LEADER / PASTOR / ADMIN
 * GET    /expenses/submitter/{n}              LEADER / PASTOR / ADMIN
 * GET    /expenses (date range)               LEADER / PASTOR / ADMIN
 * GET    /expenses/pending                    LEADER / PASTOR / ADMIN
 * GET    /expenses/overdue                    LEADER / PASTOR / ADMIN
 * GET    /expenses/summary                    PASTOR / ADMIN
 * PATCH  /expenses/{id}/submit                LEADER / PASTOR / ADMIN
 * PATCH  /expenses/{id}/approve               PASTOR / ADMIN
 * PATCH  /expenses/{id}/reject                PASTOR / ADMIN
 * PATCH  /expenses/{id}/pay                   ADMIN
 * PATCH  /expenses/{id}/cancel                LEADER / PASTOR / ADMIN
 *
 * ── REPORTS ─────────────────────────────────────────────────────────────────
 * GET    /reports/**                          PASTOR / ADMIN
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class FinanceSecurityConfig {

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
                        .requestMatchers("/actuator/health", "/actuator/info",
                                "/actuator/prometheus").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()

                        // ── Reports — PASTOR and ADMIN only ─────────────────────────
                        .requestMatchers("/api/v1/reports/**")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Finance summary — PASTOR and ADMIN only ──────────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/finance/summary")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Expenses: pay — ADMIN only ───────────────────────────────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/expenses/*/pay")
                        .hasRole("ADMIN")

                        // ── Expenses: approve/reject/summary — PASTOR and ADMIN ──────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/expenses/*/approve")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/expenses/*/reject")
                        .hasAnyRole("PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/expenses/summary")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Donations: receipt — PASTOR and ADMIN ────────────────────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/donations/*/receipt")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Offerings: verify — PASTOR and ADMIN ─────────────────────
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/offerings/*/verify")
                        .hasAnyRole("PASTOR", "ADMIN")

                        // ── Expenses: create/read/submit/cancel — LEADER and above ───
                        .requestMatchers(HttpMethod.POST,  "/api/v1/expenses")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/expenses/status/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/expenses/category/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/expenses/submitter/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/expenses/pending")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/expenses/overdue")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/expenses/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/expenses/*/submit")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/expenses/*/cancel")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Tithes: record — LEADER and above ────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/tithes")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Tithes: date range list — LEADER and above ───────────────
                        .requestMatchers(HttpMethod.GET, "/api/v1/tithes")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Tithes: member history/total — any authenticated (ownership in controller)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tithes/member/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tithes/**").authenticated()

                        // ── Donations: record, list — LEADER and above ───────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/donations")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/v1/donations/category/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/v1/donations")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/v1/donations/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        // ── Donations: member history — any authenticated (ownership in controller)
                        .requestMatchers(HttpMethod.GET, "/api/v1/donations/member/**").authenticated()

                        // ── Offerings: all — LEADER and above ────────────────────────
                        .requestMatchers("/api/v1/offerings/**")
                        .hasAnyRole("LEADER", "PASTOR", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}