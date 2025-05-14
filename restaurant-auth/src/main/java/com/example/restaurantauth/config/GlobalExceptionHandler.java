//package com.example.restaurantauth.config;
//
//import com.example.restaurantauth.exception.AccountNotVerifiedException;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.springframework.http.*;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationExceptions(
//            MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return ResponseEntity.badRequest().body(errors);
//    }
//
//    @ExceptionHandler(AccountNotVerifiedException.class)
//    public ResponseEntity<ErrorResponse> handleAccountNotVerified(AccountNotVerifiedException ex) {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                .body(new ErrorResponse(
//                        "ACCOUNT_NOT_VERIFIED",
//                        ex.getMessage(),
//                        HttpStatus.FORBIDDEN.value()
//                ));
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(new ErrorResponse(
//                        "INVALID_CREDENTIALS",
//                        ex.getMessage(),
//                        HttpStatus.UNAUTHORIZED.value()
//                ));
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ErrorResponse(
//                        "INTERNAL_ERROR",
//                        "An error occurred: " + ex.getMessage(),
//                        HttpStatus.INTERNAL_SERVER_ERROR.value()
//                ));
//    }
//
//    @Data
//    @AllArgsConstructor
//    public static class ErrorResponse {
//        private String errorCode;
//        private String message;
//        private int status;
//    }
//}