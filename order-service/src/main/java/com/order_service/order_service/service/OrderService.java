package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.*;
import com.order_service.order_service.model.Coordinates;
import com.order_service.order_service.model.CustomerLocation;
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
    private final CustomerLocationService customerLocationService;

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

           // total += orderedItem.getSubtotal();
            orderedItems.add(orderedItem);
        }

        Coordinates restaurantCoordinates = restaurant.getRestaurantCoordinates(); // ✅

        CustomerLocation location = customerLocationService.getLocationByUser(token); // ✅
        Coordinates customerCoordinates = new Coordinates(location.getLatitude(), location.getLongitude()); // ✅

        Order order = Order.builder()
                .userId(userId)
                .restaurantId(request.getRestaurantId())
                .items(orderedItems)
                .totalPrice(request.getTotalPrice())
                .status("PLACED")
                .restaurantCoordinates(restaurantCoordinates)
                .customerCoordinates(customerCoordinates)
                .deliveryCharges(request.getDeliveryCharges())
                .build();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

}
