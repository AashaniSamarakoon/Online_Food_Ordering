package com.order_service.order_service.controller;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.OrderHistoryResponse;
import com.order_service.order_service.dto.OrderRequest;
import com.order_service.order_service.dto.OrderResponse;
import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.repository.OrderRepository;
import com.order_service.order_service.service.OrderService;
import com.order_service.order_service.service.RestaurantService;
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
    private final RestaurantService restaurantService;
    private final JwtUtil jwtUtil;

    @PostMapping("/new")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request,
                                                    @RequestHeader("Authorization") String token) {
        OrderResponse response = orderService.placeOrder(request, token);
        return ResponseEntity.ok(response);
    }

//    @GetMapping
//    public ResponseEntity<List<Order>> getOrders(@RequestHeader("Authorization") String token) {
//        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        List<Order> orders = orderRepository.findByUserId(userId);
//        return ResponseEntity.ok(orders);
//    }

    @GetMapping
    public ResponseEntity<List<OrderHistoryResponse>> getOrders(@RequestHeader("Authorization") String token) {
        List<OrderHistoryResponse> history = orderService.getOrderHistory(token);
        return ResponseEntity.ok(history);
    }


    //  endpoint to fetch restaurants via RestaurantClient
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        List<RestaurantResponse> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable("id") Long id) {
        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    // Public endpoint to get all orders
    @GetMapping("/public")
    public ResponseEntity<List<OrderResponse>> getAllOrdersPublic() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // Public endpoint to get order by ID
    @GetMapping("/public/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByIdPublic(@PathVariable("orderId") Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return ResponseEntity.ok(OrderResponse.from(order));
    }

}
