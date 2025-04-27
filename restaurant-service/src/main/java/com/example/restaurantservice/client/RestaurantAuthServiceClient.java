package com.example.restaurantservice.client;



import com.example.restaurantservice.dto.RestaurantRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "restaurant-auth", url = "${restaurant-auth.url}")
public interface RestaurantAuthServiceClient {



    // Update restaurant admin information
    @PutMapping("/api/auth/admins/{id}")
    RestaurantRequest updateRestaurantAdmin(
            @PathVariable Long id,
            @RequestBody RestaurantRequest updateRequest);

    // Verify restaurant admin account
    @PatchMapping("/api/auth/admins/{id}/verify")
    RestaurantRequest verifyRestaurantAdmin(@PathVariable Long id);



    // Check if email exists
    @GetMapping("/api/auth/admins/exists/{email}")
    boolean checkEmailExists(@PathVariable String email);

    // Update password
    @PatchMapping("/api/auth/admins/{id}/password")
    void updatePassword(
            @PathVariable Long id,
            @RequestParam String newPassword);
}