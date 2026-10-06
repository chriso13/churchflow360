package com.churchflow360.eventservice.security;

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
 * Reads the X-Auth-* headers injected by the API Gateway and populates
 * the Spring Security context. No JWT re-validation — the gateway already
 * validated the token before forwarding the request.
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

        if (username != null && !username.isBlank()
                && role != null && !role.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            GatewayAuthPrincipal principal = GatewayAuthPrincipal.builder()
                    .userId(userId)
                    .username(username)
                    .role(role)
                    .email(email)
                    .membershipNumber(membershipNumber)
                    .build();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            principal, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authenticated via gateway headers: {} [{}]", username, role);
        }

        chain.doFilter(request, response);
    }
}