package com.churchflow360.pastoralservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
                    template.header("X-Auth-User-Id",  "pastoral-meeting-service");
                    template.header("X-Auth-Username", "pastoral-meeting-service");
                    template.header("X-Auth-Role",     "ADMIN");
                    template.header("X-Auth-Email",    "system@rehoboth360.org");
                    return;
                }

                HttpServletRequest request = attrs.getRequest();
                copyHeader(template, request, "X-Auth-User-Id");
                copyHeader(template, request, "X-Auth-Username");
                copyHeader(template, request, "X-Auth-Role");
                copyHeader(template, request, "X-Auth-Email");
                copyHeader(template, request, "X-Auth-Membership-Number");
                copyHeader(template, request, "X-Auth-Full-Name");

                if (request.getHeader("X-Auth-User-Id") == null) {
                    template.header("X-Auth-User-Id",  "pastoral-meeting-service");
                    template.header("X-Auth-Username", "pastoral-meeting-service");
                    template.header("X-Auth-Role",     "ADMIN");
                    template.header("X-Auth-Email",    "system@rehoboth360.org");
                }
            }

            private void copyHeader(RequestTemplate t, HttpServletRequest r, String name) {
                String value = r.getHeader(name);
                if (value != null && !value.isBlank()) t.header(name, value);
            }
        };
    }
}
