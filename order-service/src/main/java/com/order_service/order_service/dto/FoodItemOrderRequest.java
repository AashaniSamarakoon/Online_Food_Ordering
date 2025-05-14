package com.order_service.order_service.dto;

import lombok.Data;

@Data
public class FoodItemOrderRequest {
    private Long foodItemId;
    private Integer quantity;
}

