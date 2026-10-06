package com.churchflow360.eventservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("360 Church Flow — Event Service API")
                        .description("Church event scheduling, RSVP management and Kafka notifications")
                        .version("1.0.0"));
    }
}