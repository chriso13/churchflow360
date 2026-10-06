package com.churchflow360.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "churchflow360.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long   accessTokenExpiryMs  = 86400000L;   // 24h
    private long   refreshTokenExpiryMs = 604800000L;  // 7d
    private String issuer = "360-church-flow";
}