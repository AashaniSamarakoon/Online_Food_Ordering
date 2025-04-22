package com.order_service.order_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long restaurantId;
    private List<FoodItemOrderRequest> items;
}
