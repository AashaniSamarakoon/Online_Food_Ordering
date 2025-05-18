package com.example.restaurantservice.controller;

import com.example.restaurantservice.model.Order;
import com.example.restaurantservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class OrderIntegrationController {

    private final OrderService orderService;

    @PostMapping("/neworder")
    public ResponseEntity<?> receiveNewOrder(@RequestBody Order order) {
        try {
            log.info("Received new order from order-service for restaurant: {}", order.getRestaurantName());
            Order savedOrder = orderService.saveExternalOrder(order);
            log.info("Successfully saved order with ID: {}", savedOrder.getOrderId());
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            log.error("Failed to process order: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing order: " + e.getMessage());
        }
    }
}