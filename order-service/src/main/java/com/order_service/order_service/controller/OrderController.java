package com.order_service.order_service.controller;

import com.order_service.order_service.dto.OrderRequest;
import com.order_service.order_service.dto.OrderResponse;
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
}
