package com.example.restaurantservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private int status;
    private Map<String, String> errors;

    public ErrorResponse(LocalDateTime timestamp, String message, int status, Map<String, String> errors) {
        this.timestamp = timestamp;
        this.message = message;
        this.status = status;
        this.errors = errors;
    }
}