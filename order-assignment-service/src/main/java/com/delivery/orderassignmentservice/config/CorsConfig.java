package com.delivery.orderassignmentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials
        config.setAllowCredentials(true);

        // Allow specific origins or use patterns
        // Option 1: Specific origins
        // config.addAllowedOrigin("http://localhost:19006");
        // config.addAllowedOrigin("http://192.168.1.159:19006");

        // Option 2: Use patterns (Spring Boot 2.4.0+)
        config.addAllowedOriginPattern("*");

        // Standard CORS headers
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // SockJS and WebSocket specific headers
        config.addExposedHeader("SockJS-Version");
        config.addExposedHeader("heart-beat");
        config.addExposedHeader("accept-version");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}