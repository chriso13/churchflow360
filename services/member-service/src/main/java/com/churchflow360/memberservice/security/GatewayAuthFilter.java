package com.churchflow360.memberservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the identity headers injected by the API Gateway's AuthFilter
 * and populates the Spring Security context.
 *
 * <p>The gateway already validated the JWT — the member-service trusts
 * the headers and does not re-validate the token. This is the
 * standard microservice security pattern: validate once at the edge,
 * propagate identity inward as trusted headers.
 *
 * <p>Headers consumed:
 * <ul>
 *   <li>{@code X-Auth-User-Id}           — user UUID from auth-service</li>
 *   <li>{@code X-Auth-Username}          — username</li>
 *   <li>{@code X-Auth-Role}              — MEMBER | LEADER | PASTOR | ADMIN</li>
 *   <li>{@code X-Auth-Email}             — email</li>
 *   <li>{@code X-Auth-Membership-Number} — membership number (may be empty)</li>
 * </ul>
 */
@Component
@Slf4j
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String userId           = request.getHeader("X-Auth-User-Id");
        String username         = request.getHeader("X-Auth-Username");
        String role             = request.getHeader("X-Auth-Role");
        String email            = request.getHeader("X-Auth-Email");
        String membershipNumber = request.getHeader("X-Auth-Membership-Number");

        // If gateway headers are present, authenticate the request
        if (username != null && !username.isBlank()
                && role != null && !role.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Build a principal that controllers can access
            GatewayAuthPrincipal principal = GatewayAuthPrincipal.builder()
                    .userId(userId)
                    .username(username)
                    .role(role)
                    .email(email)
                    .membershipNumber(membershipNumber)
                    .build();

            // Spring Security expects "ROLE_" prefix
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authenticated via gateway headers: {} [{}]", username, role);
        }

        chain.doFilter(request, response);
    }
}