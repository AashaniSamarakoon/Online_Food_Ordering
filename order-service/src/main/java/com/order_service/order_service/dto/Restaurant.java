package com.order_service.order_service.dto;

import com.order_service.order_service.model.Coordinates;
import lombok.Data;

import java.util.List;

@Data
public class Restaurant {
    private Long id;
    private String name;
    private String address;
    private boolean isOpen;
    private Coordinates restaurantCoordinates;
    private List<FoodItem> items;
}

