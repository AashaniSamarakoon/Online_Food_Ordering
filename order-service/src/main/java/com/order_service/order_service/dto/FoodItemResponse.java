package com.order_service.order_service.dto;

import lombok.Data;

@Data
public class FoodItemResponse {
    private Long id;
    private String name;
    private String description;
    private double price;
    private boolean available;
}


