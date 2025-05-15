package com.example.restaurantservice.config;

import com.example.restaurantservice.exception.AuthException;
//import com.example.restaurantservice.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error response from Feign client: {} - {}", response.status(), methodKey);

        // Try to read error body for more information
        String errorBody = getErrorBody(response);
        log.error("Error body: {}", errorBody);

        switch (response.status()) {
            case 400:
                return new RuntimeException("Bad request to restaurant-auth service: " + errorBody);
            case 401:
                return new AuthException("Unauthorized access to restaurant-auth service: " + errorBody);
            case 403:
                return new AuthException("Forbidden access to restaurant-auth service: " + errorBody);
//            case 404:
//                return new ResourceNotFoundException("Restaurant not found in auth service: " + errorBody);
            default:
                return new RuntimeException("Error in restaurant-auth service: " + errorBody);
        }
    }

    private String getErrorBody(Response response) {
        if (response.body() == null) {
            return "No error body";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Error reading response body", e);
            return "Error reading response body";
        }
    }
}