package com.example.restaurantservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class ServiceAuthConfig {

    @Value("${service-to-service.username}")
    private String serviceUsername;

    @Value("${service-to-service.password}")
    private String servicePassword;

    @Value("${restaurant-auth.url}")
    private String authServiceUrl;

    @Bean
    public RequestInterceptor authRequestInterceptor() {
        return requestTemplate -> {
            // If endpoint contains "by-owner", use the user's token from the incoming request
            // instead of generating a service token
            if (!requestTemplate.url().contains("/by-owner/")) {
                String serviceToken = getServiceToken();
                requestTemplate.header("Authorization", serviceToken);
            }
        };
    }

    public String getServiceToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> authRequest = Map.of(
                    "email", serviceUsername,
                    "password", servicePassword
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(authRequest, headers);

            String loginEndpoint = authServiceUrl + "/api/auth/login";
            ResponseEntity<Map> response = restTemplate.exchange(
                    loginEndpoint,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("token")) {
                return "Bearer " + response.getBody().get("token");
            } else {
                throw new RuntimeException("Unable to obtain service token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error obtaining service token: " + e.getMessage(), e);
        }
    }
}