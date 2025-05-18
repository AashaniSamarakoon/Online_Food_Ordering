//package com.order_service.order_service.service;
//
//import com.order_service.order_service.client.OrderAssignmentClient;
//import com.order_service.order_service.client.RestaurantClient;
//import com.order_service.order_service.client.UserClient;
//import com.order_service.order_service.dto.*;
//import com.order_service.order_service.model.Coordinates;
//import com.order_service.order_service.model.CustomerLocation;
//import com.order_service.order_service.model.Order;
//import com.order_service.order_service.model.OrderedItem;
//import com.order_service.order_service.repository.OrderRepository;
//import com.order_service.order_service.util.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final RestaurantClient restaurantClient;
//    private final OrderAssignmentClient orderAssignmentClient;
//    private final OrderRepository orderRepository;
//    private final JwtUtil jwtUtil;
//    private final CustomerLocationService customerLocationService;
//    private final UserClient userClient;
//    private final NotificationService notificationService;
//    private final RestaurantService restaurantService;
//
//
//    public OrderResponse placeOrder(OrderRequest request, String token) {
//        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        RestaurantResponse restaurant = restaurantService.getRestaurantById(request.getRestaurantId());
//        String restaurantName = restaurant.getName();
//        String restaurantAddress = restaurant.getAddress();
//
//        double total = 0;
//        List<OrderedItem> orderedItems = new ArrayList<>();
//
//        for (FoodItemOrderRequest itemRequest : request.getItems()) {
//            FoodItemResponse item = restaurant.getItems().stream()
//                    .filter(i -> i.getId().equals(itemRequest.getFoodItemId()))
//                    .findFirst()
//                    .orElseThrow(() -> new RuntimeException("Item not found or unavailable"));
//
//            OrderedItem orderedItem = OrderedItem.builder()
//                    .foodItemId(item.getId())
//                    .name(item.getName())
//                    .description(item.getDescription())
//                    .category(item.getCategory())
//                    .imageUrl(item.getImageUrl())
//                    .price(item.getPrice())
//                    .quantity(itemRequest.getQuantity())
//                    .subtotal(item.getPrice() * itemRequest.getQuantity())
//                  //  .available(item.isAvailable())
//                    .build();
//
//           // total += orderedItem.getSubtotal();
//            orderedItems.add(orderedItem);
//        }
//
//        Coordinates restaurantCoordinates = restaurant.getRestaurantCoordinates(); // ✅
//
//        CustomerLocation location = customerLocationService.getLocationByUser(token); // ✅
//        Coordinates customerCoordinates = new Coordinates(location.getLatitude(), location.getLongitude()); // ✅
//
////        Order order = Order.builder()
////                .userId(userId)
////                .restaurantId(request.getRestaurantId())
////                .items(orderedItems)
////                .totalPrice(request.getTotalPrice())
////                .status("PLACED")
////                .restaurantCoordinates(restaurantCoordinates)
////                .customerCoordinates(customerCoordinates)
////                .deliveryCharges(request.getDeliveryCharges())
////                .build();
////
////        Order savedOrder = orderRepository.save(order);
//
//        Map<String, Object> userProfile = userClient.getUserProfile(token);
//        String email = (String) userProfile.get("email");
//        String phoneNumber = (String) userProfile.get("phoneNumber");
//        String address = (String) userProfile.get("addressLine1");
//        String username = (String) userProfile.get("username");
//
//        Order order = Order.builder()
//                .userId(userId)
//                .restaurantId(request.getRestaurantId())
//                .restaurantName(restaurantName)
//                .items(orderedItems)
//                .totalPrice(request.getTotalPrice())
//                .status("PLACED")
//                .restaurantCoordinates(restaurantCoordinates)
//                .customerCoordinates(customerCoordinates)
//                .deliveryCharges(request.getDeliveryCharges())
//                .email(email)
//                .phoneNumber(phoneNumber)
//                .address(address)
//                .username(username)
//                .restaurantAddress(restaurantAddress)
//                .build();
//
//        Order savedOrder = orderRepository.save(order);
//
//
//        //orderAssignmentClient.processOrderAssignment(savedOrder.getId());
//        restaurantClient.notifyNewOrder(savedOrder);
//        notificationService.sendOrderConfirmation(userProfile, savedOrder);
//        return OrderResponse.from(savedOrder);
//    }
//
//    public List<OrderHistoryResponse> getOrderHistory(String token) {
//        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        List<Order> orders = orderRepository.findByUserId(userId);
//
//        return orders.stream().map(order -> {
//            RestaurantResponse restaurant = restaurantService.getRestaurantById(order.getRestaurantId());
//            return OrderHistoryResponse.builder()
//                    .orderId(order.getId())
//                    .restaurantId(order.getRestaurantId())
//                    .restaurantName(restaurant.getName())
//                    .items(order.getItems())
//                    .totalPrice(order.getTotalPrice())
//                    .status(order.getStatus())
//                    .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
//                    .build();
//        }).toList();
//    }
//
//
//}


package com.order_service.order_service.service;

import com.order_service.order_service.client.OrderAssignmentClient;
import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.client.UserClient;
import com.order_service.order_service.dto.*;
import com.order_service.order_service.mapper.OrderMapper;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RestaurantClient restaurantClient;
    private final OrderAssignmentClient orderAssignmentClient;
    private final OrderRepository orderRepository;
    private final JwtUtil jwtUtil;
    private final CustomerLocationService customerLocationService;
    private final UserClient userClient;
    private final NotificationService notificationService;
    private final RestaurantService restaurantService;

    public OrderResponse placeOrder(OrderRequest request, String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        RestaurantResponse restaurant = restaurantService.getRestaurantById(request.getRestaurantId());
        String restaurantName = restaurant.getName();
        String restaurantAddress = restaurant.getAddress();

        List<OrderedItem> orderedItems = new ArrayList<>();

        for (FoodItemOrderRequest itemRequest : request.getItems()) {
            FoodItemResponse item = restaurant.getItems().stream()
                    .filter(i -> i.getId().equals(itemRequest.getFoodItemId()))
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
                    .build();

            orderedItems.add(orderedItem);
        }

        Coordinates restaurantCoordinates = restaurant.getRestaurantCoordinates();

        CustomerLocation location = customerLocationService.getLocationByUser(token);
        Coordinates customerCoordinates = new Coordinates(location.getLatitude(), location.getLongitude());

        Map<String, Object> userProfile = userClient.getUserProfile(token);
        String email = (String) userProfile.get("email");
        String phoneNumber = (String) userProfile.get("phoneNumber");
        String address = (String) userProfile.get("addressLine1");
        String username = (String) userProfile.get("username");

        Order order = Order.builder()
                .userId(userId)
                .restaurantId(request.getRestaurantId())
                .restaurantName(restaurantName)
                .items(orderedItems)
                .totalPrice(request.getTotalPrice())
                .status("PLACED")
                .restaurantCoordinates(restaurantCoordinates)
                .customerCoordinates(customerCoordinates)
                .deliveryCharges(request.getDeliveryCharges())
                .email(email)
                .phoneNumber(phoneNumber)
                .address(address)
                .username(username)
                .restaurantAddress(restaurantAddress)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Convert entity to DTO before sending to restaurant service
        OrderDTO orderDTO = OrderMapper.toDTO(savedOrder);
        restaurantClient.notifyNewOrder(orderDTO);
//        orderAssignmentClient.processOrderAssignment(savedOrder.getId());

        notificationService.sendOrderConfirmation(userProfile, savedOrder);
        return OrderResponse.from(savedOrder);
    }

    public List<OrderHistoryResponse> getOrderHistory(String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream().map(order -> {
            RestaurantResponse restaurant = restaurantService.getRestaurantById(order.getRestaurantId());
            return OrderHistoryResponse.builder()
                    .orderId(order.getId())
                    .restaurantId(order.getRestaurantId())
                    .restaurantName(restaurant.getName())
                    .items(order.getItems())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                    .build();
        }).toList();
    }
}