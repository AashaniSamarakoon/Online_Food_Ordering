package com.restaurant_service.restaurant_service.controller;

import com.restaurant_service.restaurant_service.model.FoodItem;
import com.restaurant_service.restaurant_service.model.Restaurant;
import com.restaurant_service.restaurant_service.repository.FoodItemRepository;
import com.restaurant_service.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;

    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<FoodItem> addItem(@PathVariable Long restaurantId, @RequestBody FoodItem item) {
        return restaurantRepository.findById(restaurantId).map(restaurant -> {
            item.setRestaurant(restaurant);
            return ResponseEntity.ok(foodItemRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<FoodItem> getItemsByRestaurant(@PathVariable Long restaurantId) {
        return foodItemRepository.findByRestaurantId(restaurantId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodItem> updateItem(@PathVariable Long id, @RequestBody FoodItem updated) {
        return foodItemRepository.findById(id).map(item -> {
            item.setName(updated.getName());
            item.setDescription(updated.getDescription());
            item.setPrice(updated.getPrice());
            item.setAvailable(updated.isAvailable());
            return ResponseEntity.ok(foodItemRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        foodItemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

