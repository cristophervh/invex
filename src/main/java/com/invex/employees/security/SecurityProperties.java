package com.invex.employees.security;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

/**
 * Values come from environment variables (see application.yml). The application
 * refuses to start if any of them is missing or the JWT secret is too short.
 */
@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @NotBlank @Size(min = 32, message = "must be at least 32 characters for HS256") String jwtSecret,
        @Positive long jwtExpirationMinutes,
        @NotBlank String adminUsername,
        @NotBlank String adminPassword,
        @NotBlank String readerUsername,
        @NotBlank String readerPassword) {
}
