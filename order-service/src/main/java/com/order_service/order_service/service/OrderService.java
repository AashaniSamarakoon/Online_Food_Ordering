package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.*;
import com.order_service.order_service.model.Coordinates;
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
    private final JwtUtil jwtUtil;

    public OrderResponse placeOrder(OrderRequest request, String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
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
                    .description(item.getDescription())
                    .category(item.getCategory())
                    .imageUrl(item.getImageUrl())
                    .price(item.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .subtotal(item.getPrice() * itemRequest.getQuantity())
                    .available(item.isAvailable())
                    .build();

            total += item.getPrice() * itemRequest.getQuantity();
            orderedItems.add(orderedItem);
        }

        Coordinates restaurantCoordinates = restaurant.getRestaurantCoordinates(); // ✅ Now this will work


        Coordinates customerCoordinates = request.getCustomerCoordinates(); // sent from frontend

        Order order = Order.builder()
                .userId(userId)
                .restaurantId(request.getRestaurantId())
                .items(orderedItems)
                .totalPrice(total)
                .status("PLACED")
                .restaurantCoordinates(restaurantCoordinates)
                .customerCoordinates(customerCoordinates)
                .build();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

}
