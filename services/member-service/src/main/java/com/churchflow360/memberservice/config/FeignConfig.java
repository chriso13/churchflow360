package com.churchflow360.memberservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign configuration for service-to-service calls.
 *
 * When member-service calls notification-service directly (lb://),
 * the gateway is bypassed so there is no AuthFilter to inject X-Auth-* headers.
 * This interceptor copies those headers from the incoming HTTP request so
 * notification-service's JwtAuthFilter (gateway-mode) accepts the call.
 */
@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor xAuthHeaderInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attrs == null) {
                    // Async/event context — inject minimal service identity
                    template.header("X-Auth-User-Id",   "member-service");
                    template.header("X-Auth-Username",  "member-service");
                    template.header("X-Auth-Role",      "ADMIN");
                    template.header("X-Auth-Email",     "system@rehoboth360.org");
                    return;
                }

                HttpServletRequest request = attrs.getRequest();

                // Forward X-Auth-* headers injected by the gateway
                copyHeader(template, request, "X-Auth-User-Id");
                copyHeader(template, request, "X-Auth-Username");
                copyHeader(template, request, "X-Auth-Role");
                copyHeader(template, request, "X-Auth-Email");
                copyHeader(template, request, "X-Auth-Membership-Number");
                copyHeader(template, request, "X-Auth-Full-Name");

                // Fallback: if gateway headers are absent, inject service identity
                if (request.getHeader("X-Auth-User-Id") == null) {
                    template.header("X-Auth-User-Id",  "member-service");
                    template.header("X-Auth-Username", "member-service");
                    template.header("X-Auth-Role",     "ADMIN");
                    template.header("X-Auth-Email",    "system@rehoboth360.org");
                }
            }

            private void copyHeader(RequestTemplate template,
                                    HttpServletRequest request,
                                    String headerName) {
                String value = request.getHeader(headerName);
                if (value != null && !value.isBlank()) {
                    template.header(headerName, value);
                }
            }
        };
    }
}
