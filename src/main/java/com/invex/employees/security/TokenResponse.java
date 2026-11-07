package com.invex.employees.security;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
