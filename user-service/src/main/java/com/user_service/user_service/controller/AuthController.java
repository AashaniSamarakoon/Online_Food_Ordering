package com.user_service.user_service.controller;

import com.user_service.user_service.model.User;
import com.user_service.user_service.repository.UserRepository;
import com.user_service.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.user_service.user_service.dto.UpdatePhoneRequest;
import com.user_service.user_service.dto.VerifyPhoneRequest;
import com.user_service.user_service.service.SmsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User existing = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(user.getPassword(), existing.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtUtil.generateTokenWithUserId(existing.getId());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        Map<String, Object> profile = Map.of(
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "username", user.getUsername(),
                "addressLine1", user.getAddressLine1()
        );

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/update-phone")
    public ResponseEntity<?> updatePhoneNumber(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdatePhoneRequest request) {

        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = String.valueOf((int) (Math.random() * 9000) + 1000); // 4-digit code
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPhoneVerificationCode(code);
        user.setIsPhoneVerified(false);

        smsService.sendSms(request.getPhoneNumber(), "Your verification code is: " + code);

        userRepository.save(user);
        return ResponseEntity.ok("Verification code sent");
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyPhoneNumber(
            @RequestHeader("Authorization") String token,
            @RequestBody VerifyPhoneRequest request) {

        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPhoneNumber().equals(request.getPhoneNumber()) &&
                user.getPhoneVerificationCode().equals(request.getVerificationCode())) {
            user.setIsPhoneVerified(true);
            user.setPhoneVerificationCode(null);
            userRepository.save(user);
            return ResponseEntity.ok("Phone number verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid verification code");
        }
    }

    @GetMapping("/userprofile")
    public ResponseEntity<?> getuserProfile(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        Map<String, Object> profile = Map.of(
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "isPhoneVerified", user.getIsPhoneVerified(),
                "username", user.getUsername(),
                "addressLine1", user.getAddressLine1()
        );

        return ResponseEntity.ok(profile);
    }

}
