package com.churchflow360.attendanceservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        // TODO: replace with SecurityContextHolder when auth is integrated
        return () -> Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}