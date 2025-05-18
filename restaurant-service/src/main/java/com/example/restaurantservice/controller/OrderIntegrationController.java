//package com.example.restaurantservice.controller;
//
//import com.example.restaurantservice.model.Order;
//import com.example.restaurantservice.service.OrderService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//@Slf4j
//public class OrderIntegrationController {
//
//    private final OrderService orderService;
//
//    @PostMapping("/neworder")
//    public ResponseEntity<?> receiveNewOrder(@RequestBody Order order) {
//        try {
//            log.info("Received new order from order-service for restaurant: {}", order.getRestaurantName());
//
//            // Log details for debugging
//            log.debug("Order details: userId={}, restaurantId={}, items count={}, total price={}",
//                    order.getUserId(),
//                    order.getRestaurantId(),
//                    order.getItems() != null ? order.getItems().size() : 0,
//                    order.getTotalPrice());
//
//            Order savedOrder = orderService.saveExternalOrder(order);
//            log.info("Successfully saved order with ID: {}", savedOrder.getOrderId());
//            return ResponseEntity.ok(savedOrder);
//        } catch (Exception e) {
//            log.error("Failed to process order: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body("Error processing order: " + e.getMessage());
//        }
//    }
//}

package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.OrderDTO;
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
    public ResponseEntity<?> receiveNewOrder(@RequestBody OrderDTO orderDTO) {
        try {
            log.info("Received new order from order-service for restaurant: {}", orderDTO.getRestaurantName());

            // Log details for debugging
            log.debug("Order details: userId={}, restaurantId={}, items count={}, total price={}",
                    orderDTO.getUserId(),
                    orderDTO.getRestaurantId(),
                    orderDTO.getItems() != null ? orderDTO.getItems().size() : 0,
                    orderDTO.getTotalPrice());

            Order savedOrder = orderService.saveExternalOrder(orderDTO);
            log.info("Successfully saved order with ID: {}", savedOrder.getOrderId());
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            log.error("Failed to process order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing order: " + e.getMessage());
        }
    }
}