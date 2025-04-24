package com.example.restaurantservice.config;

public class SecurityConstants {
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String[] PUBLIC_URLS = {
            "/api/restaurants/public/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/actuator/health"
    };
}