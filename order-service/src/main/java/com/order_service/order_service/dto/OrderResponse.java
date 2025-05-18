package com.order_service.order_service.dto;

import com.order_service.order_service.model.Coordinates;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.model.OrderedItem;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private List<OrderedItem> items;
    private Long restaurantId;
    private Coordinates restaurantCoordinates;
    private Coordinates customerCoordinates;
    private Double deliveryCharges;
    private Double totalPrice;
    private String status;
    private String username;
    private String email;
    private String address;
    private String phoneNumber;
    private String restaurantName;
    private String restaurantAddress;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getItems())
                .restaurantId(order.getRestaurantId())
                .restaurantCoordinates(order.getRestaurantCoordinates())
                .customerCoordinates(order.getCustomerCoordinates())
                .deliveryCharges(order.getDeliveryCharges())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .username(order.getUsername())
                .email(order.getEmail())
                .address(order.getAddress())
                .phoneNumber(order.getPhoneNumber())
                .restaurantName(order.getRestaurantName())
                .restaurantAddress(order.getRestaurantAddress())
                .build();
    }
}
