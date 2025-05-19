package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.*;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.service.OrderManagementService;
import com.example.restaurantservice.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderManagementService orderManagementService;
    private final RestaurantService restaurantService;

    /**
     * Gets restaurant information for the current user
     */
    private RestaurantResponse getRestaurantForCurrentUser(String ownerId) {
        try {
            return restaurantService.getRestaurantByOwner(ownerId);
        } catch (RestaurantNotFoundException ex) {
            log.info("Restaurant not found for owner: {}. Trying to sync from Auth Service", ownerId);
            return restaurantService.getRestaurantByOwner(ownerId);
        }
    }

    @GetMapping("/my-restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public List<OrderListResponseDTO> getMyRestaurantOrders(Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);
        return orderManagementService.getOrdersByRestaurant(restaurant.getId());
    }

    @GetMapping("/my-restaurant/status/{status}")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public List<OrderListResponseDTO> getMyRestaurantOrdersByStatus(
            @PathVariable String status,
            Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);
        return orderManagementService.getOrdersByRestaurantAndStatus(restaurant.getId(), status);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<OrderDetailResponseDTO> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);

        // Check if the order belongs to this restaurant
        OrderDetailResponseDTO order = orderManagementService.getOrderById(orderId, restaurant.getId());

        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<OrderDetailResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateDTO statusUpdate,
            Authentication authentication) {

        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);

        try {
            OrderDetailResponseDTO updatedOrder = orderManagementService.updateOrderStatus(
                    orderId,
                    statusUpdate.getStatus(),
                    restaurant.getId()
            );

            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            log.error("Error updating order status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<OrderStatusDTO> getOrderStatus(
            @PathVariable Long orderId,
            Authentication authentication) {

        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);

        OrderStatusDTO status = orderManagementService.getOrderStatus(orderId, restaurant.getId());

        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}