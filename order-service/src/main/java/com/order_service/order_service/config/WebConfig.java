package com.order_service.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // allow all paths
                        .allowedOrigins("http://localhost:5173") // your frontend URL
                        .allowedMethods("*") // GET, POST, PUT, DELETE
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}


