//package com.example.restaurantauth.controller;
//
//import com.example.restaurantauth.dto.*;
//import com.example.restaurantauth.service.AuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//@Validated
//@Slf4j
//public class AuthController {
//    private final AuthService authService;
//
//    @PostMapping(
//            value = "/register",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<RegisterResponse> register(
//            @Valid @RequestBody RegisterRequest request) {
//        RegisterResponse response = authService.register(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping(
//            value = "/authenticate",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<AuthResponse> authenticate(
//            @Valid @RequestBody AuthRequest request) {
//        AuthResponse response = authService.authenticate(request);
//        return ResponseEntity.ok(response);
//    }
//}

package com.example.restaurantauth.controller;

import com.example.restaurantauth.dto.*;
import com.example.restaurantauth.exception.ResourceNotFoundException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/authenticate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkVerificationStatus(
            @RequestParam String email) {
        RestaurantAdmin admin = authService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Map<String, Object> response = new HashMap<>();
        response.put("email", admin.getEmail());
        response.put("verified", admin.isVerified());
        response.put("restaurantName", admin.getRestaurantName());
        response.put("message", admin.isVerified()
                ? "Your account has been verified. You can now log in."
                : "Your account is pending verification. Please wait for admin approval.");

        return ResponseEntity.ok(response);
    }
}