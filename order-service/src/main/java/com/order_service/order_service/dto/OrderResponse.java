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
                .build();
    }
}
