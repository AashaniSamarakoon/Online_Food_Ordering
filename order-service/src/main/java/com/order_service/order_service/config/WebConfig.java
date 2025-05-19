package com.order_service.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import static org.springframework.http.CacheControl.maxAge;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // allow all paths
                        .allowedOrigins("http://localhost:5173") //  frontend URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // GET, POST, PUT, DELETE
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}


