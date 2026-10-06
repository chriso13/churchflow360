package com.churchflow360.memberservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides the current auditor for JPA @CreatedBy fields.
 * Returns a placeholder UUID until auth is wired up.
 */
@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        // TODO: replace with SecurityContextHolder when auth-service is integrated
        return () -> Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}