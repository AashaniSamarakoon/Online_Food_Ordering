package com.order_service.order_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Restaurant {
    private Long id;
    private String name;
    private String address;
    private boolean isOpen;
    private List<FoodItem> items;
}

