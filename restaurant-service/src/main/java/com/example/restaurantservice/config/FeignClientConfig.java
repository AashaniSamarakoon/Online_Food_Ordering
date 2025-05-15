package com.example.restaurantservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class FeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // Log all details for debugging
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Don't add authorization header if it's already been explicitly set
            if (requestTemplate.headers().containsKey("Authorization")) {
                return;
            }

            // Extract authorization token from current security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                String token = jwtAuth.getToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}