// dto/OrderResponse.java
package com.order_service.order_service.dto;

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
    private String username;
    private List<OrderedItem> items;
    private Long restaurantId;
    private Double totalPrice;
    private String status;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .username(order.getUsername())
                .items(order.getItems())
                .restaurantId(order.getRestaurantId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }
}
