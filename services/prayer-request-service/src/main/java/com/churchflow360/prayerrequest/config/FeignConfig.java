package com.churchflow360.prayerrequest.config;

import com.churchflow360.prayerrequest.security.JwtService;
import feign.RequestInterceptor;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

            requestTemplate.header("Authorization", authHeader);

            try {
                Claims claims = jwtService.extractAllClaims(token);
                String userId   = claims.get("userId",           String.class);
                String username = claims.getSubject();
                String role     = claims.get("role",             String.class);
                String email    = claims.get("email",            String.class);
                String mn       = claims.get("membershipNumber", String.class);
                String fullName = claims.get("fullName",         String.class);

                if (userId   != null) requestTemplate.header("X-Auth-User-Id",          userId);
                if (username != null) requestTemplate.header("X-Auth-Username",          username);
                if (role     != null) requestTemplate.header("X-Auth-Role",              role);
                if (email    != null) requestTemplate.header("X-Auth-Email",             email);
                if (mn       != null) requestTemplate.header("X-Auth-Membership-Number", mn);
                if (fullName != null) requestTemplate.header("X-Auth-Full-Name",         fullName);
            } catch (Exception ignored) {}
        };
    }
}
