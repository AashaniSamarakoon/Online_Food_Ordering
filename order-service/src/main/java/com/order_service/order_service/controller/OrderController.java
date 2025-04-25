package com.order_service.order_service.controller;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.OrderRequest;
import com.order_service.order_service.dto.OrderResponse;
import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.repository.OrderRepository;
import com.order_service.order_service.service.OrderService;
import com.order_service.order_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final JwtUtil jwtUtil;

    @PostMapping("/new")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request,
                                                    @RequestHeader("Authorization") String token) {
        OrderResponse response = orderService.placeOrder(request, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        List<Order> orders = orderRepository.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // New endpoint to fetch restaurants via RestaurantClient
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        List<RestaurantResponse> restaurants = restaurantClient.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable("id") Long id) {
        RestaurantResponse restaurant = restaurantClient.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }
}
