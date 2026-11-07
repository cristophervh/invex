package com.invex.employees.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI employeeServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Service API")
                        .version("1.0.0")
                        .description("""
                                CRUD and search for employees.

                                1. Request a token with POST /auth/token (admin or reader).
                                2. Click "Authorize" and paste the accessToken.

                                Dates use the format dd-MM-yyyy. GET endpoints need the READER or ADMIN role; \
                                POST, PUT and DELETE need ADMIN."""))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
