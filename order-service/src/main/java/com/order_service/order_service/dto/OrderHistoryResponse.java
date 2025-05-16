package com.order_service.order_service.dto;

import com.order_service.order_service.model.OrderedItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long orderId;
    private String restaurantName;
    private Long restaurantId;
    private List<OrderedItem> items;
    private double totalPrice;
    private String status;
    private String createdAt;
}
