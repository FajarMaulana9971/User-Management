package com.unictive.usermanagement.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme())
                );
    }

    private Info buildInfo() {
        return new Info()
                .title("User Management API")
                .description("""
                Enterprise-grade User & Hobby Management System.
                
                **Features:**
                - JWT Authentication with Redis Blacklist
                - RBAC (ADMIN / USER)
                - Hibernate Envers Audit Trail
                - Profile Picture Upload with Image Compression
                - Distributed Caching with Redis
                """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Fajar Anwari Maulana")
                        .email("fajar.anwarimaulana99@gmail.com"))
                .license(new License()
                        .name("Private")
                        .url("fajar.anwarimaulana99@gmail.com"));
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT token (without 'Bearer ' prefix)");
    }
}
