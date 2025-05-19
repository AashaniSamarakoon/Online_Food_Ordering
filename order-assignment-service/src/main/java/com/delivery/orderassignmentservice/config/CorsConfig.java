package com.delivery.orderassignmentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials
        config.setAllowCredentials(true);

        // Use allowed origin patterns instead of specific origins
        config.addAllowedOriginPattern("*");

        // Standard CORS headers
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // Required for WebSocket
        config.addExposedHeader("SockJS-Version");
        config.addExposedHeader("heart-beat");
        config.addExposedHeader("accept-version");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}