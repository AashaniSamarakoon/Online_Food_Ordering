package com.order_service.order_service.dto;

import lombok.Data;

@Data
public class FoodItemResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private double price;
    private boolean available;
}


