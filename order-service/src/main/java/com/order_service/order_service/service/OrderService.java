// service/OrderService.java
package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.*;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.model.OrderedItem;
import com.order_service.order_service.repository.OrderRepository;
import com.order_service.order_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RestaurantClient restaurantClient;
    private final OrderRepository orderRepository;
    private final JwtUtil jwtUtils;

    public OrderResponse placeOrder(OrderRequest request, String token) {
        String username = jwtUtils.extractUsername(token.replace("Bearer ", ""));
        RestaurantResponse restaurant = restaurantClient.getRestaurantById(request.getRestaurantId());

        double total = 0;
        List<OrderedItem> orderedItems = new ArrayList<>();

        for (FoodItemOrderRequest itemRequest : request.getItems()) {
            FoodItemResponse item = restaurant.getItems().stream()
                    .filter(i -> i.getId().equals(itemRequest.getFoodItemId()) && i.isAvailable())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item not found or unavailable"));

            OrderedItem orderedItem = OrderedItem.builder()
                    .foodItemId(item.getId())
                    .name(item.getName())
                    .price(item.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();

            total += item.getPrice() * itemRequest.getQuantity();
            orderedItems.add(orderedItem);
        }

        Order order = Order.builder()
                .username(username)
                .restaurantId(request.getRestaurantId())
                .items(orderedItems)
                .totalPrice(total)
                .status("PLACED")
                .build();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }
}
