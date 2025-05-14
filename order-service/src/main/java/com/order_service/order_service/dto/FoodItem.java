package com.order_service.order_service.dto;

import lombok.Data;

@Data
public class FoodItem {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private boolean available;
}

