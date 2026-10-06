package com.churchflow360.smallgroups.config;

import com.churchflow360.smallgroups.security.JwtService;
import feign.RequestInterceptor;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign client configuration.
 *
 * The member-service (and other downstream services) use GatewayAuthFilter —
 * they expect X-Auth-* headers that the API Gateway normally injects after
 * validating the JWT. When calling service-to-service via Feign (bypassing
 * the gateway), we must inject those same headers ourselves from the JWT.
 */
@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor gatewayHeaderInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            String authHeader = attrs.getRequest().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

            String token = authHeader.substring(7);
            if (!jwtService.isValid(token)) return;

            // Forward the raw Authorization header
            requestTemplate.header("Authorization", authHeader);

            // Extract JWT claims and inject the X-Auth-* headers that
            // GatewayAuthFilter expects on downstream services
            try {
                Claims claims = jwtService.extractAllClaims(token);
                String userId   = claims.get("userId",           String.class);
                String username = claims.getSubject();
                String role     = claims.get("role",             String.class);
                String email    = claims.get("email",            String.class);
                String mn       = claims.get("membershipNumber", String.class);
                String fullName = claims.get("fullName",         String.class);

                if (userId   != null) requestTemplate.header("X-Auth-User-Id",           userId);
                if (username != null) requestTemplate.header("X-Auth-Username",           username);
                if (role     != null) requestTemplate.header("X-Auth-Role",               role);
                if (email    != null) requestTemplate.header("X-Auth-Email",              email);
                if (mn       != null) requestTemplate.header("X-Auth-Membership-Number",  mn);
                if (fullName != null) requestTemplate.header("X-Auth-Full-Name",          fullName);
            } catch (Exception e) {
                // If claim extraction fails, just forward the Authorization header
                // The downstream service will reject it if invalid
            }
        };
    }
}