package com.example.restaurantauth.controller;


import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.service.RestaurantAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantAdminController {
    private final RestaurantAdminService adminService;

    @GetMapping("/profile")
    public String getProfile(@AuthenticationPrincipal RestaurantAdmin admin) {
        return "Welcome, " + admin.getRestaurantName() + " (" + admin.getEmail() + ")";
    }
}
