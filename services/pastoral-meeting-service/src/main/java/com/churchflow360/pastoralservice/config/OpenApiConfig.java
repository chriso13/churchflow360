package com.churchflow360.pastoralservice.config;

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
                        .title("360 Church Flow — Pastoral Meeting Service API")
                        .description("Scheduling, status workflow and confidential pastoral notes")
                        .version("1.0.0"));
    }
}