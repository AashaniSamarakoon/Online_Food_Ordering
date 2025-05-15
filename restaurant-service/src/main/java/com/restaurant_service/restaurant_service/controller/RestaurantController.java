package com.restaurant_service.restaurant_service.controller;

import com.restaurant_service.restaurant_service.model.Restaurant;
import com.restaurant_service.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    @PostMapping
    public Restaurant addRestaurant(@RequestBody Restaurant restaurant) {
        return restaurantRepository.save(restaurant);

    }

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        System.out.println("hi");
        return restaurantRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable Long id) {
        return restaurantRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant updated) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurant.setName(updated.getName());
            restaurant.setAddress(updated.getAddress());
            restaurant.setOpen(updated.isOpen());
            return ResponseEntity.ok(restaurantRepository.save(restaurant));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id) {
        restaurantRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nearby")
    public List<Restaurant> getNearbyRestaurants(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius // in km
    ) {
        return restaurantRepository.findNearbyRestaurants(lat, lng, radius);
    }
}

