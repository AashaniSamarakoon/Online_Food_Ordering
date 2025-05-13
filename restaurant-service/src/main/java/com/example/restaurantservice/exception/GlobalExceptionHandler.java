package com.example.restaurantservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiError apiError = new ApiError(
                status.value(),
                "Validation error",
                errors,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiError, status);
    }

    // Handle database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Database constraint violation", ex);

        String message = ex.getMostSpecificCause().getMessage();
        Map<String, String> details = new HashMap<>();

        // Check for null value constraint violations
        Pattern pattern = Pattern.compile("null value in column \"([\\w_]+)\" of relation");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String columnName = matcher.group(1);
            String errorMessage = "Database error: Required field '" + columnName + "' is missing";
            details.put(columnName, "This field is required but was not provided");

            ApiError apiError = new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    errorMessage,
                    details,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        // Handle unique constraint violations
        pattern = Pattern.compile("duplicate key value violates unique constraint");
        matcher = pattern.matcher(message);

        if (matcher.find()) {
            String errorMessage = "Database error: Duplicate entry found";
            details.put("constraint", message);

            ApiError apiError = new ApiError(
                    HttpStatus.CONFLICT.value(),
                    errorMessage,
                    details,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
        }

        // Generic constraint violation
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Database constraint violation occurred",
                Map.of("details", message),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ApiError> handleRestaurantNotFoundException(
            RestaurantNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<ApiError> handleMenuItemNotFoundException(
            MenuItemNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    // Only include this if you have the RestaurantAlreadyExistsException class
    @ExceptionHandler(RestaurantAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleRestaurantAlreadyExistsException(
            RestaurantAlreadyExistsException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    // Only include this if you have the AuthException class
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiError> handleAuthException(
            AuthException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Only include this if you have the ForbiddenException class
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}