package com.churchflow360.memberservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async support so MemberEmailService.sendWelcomeEmail()
 * runs in a background thread and never blocks the HTTP response.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}