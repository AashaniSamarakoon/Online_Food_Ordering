package com.order_service.order_service.dto;

import com.order_service.order_service.model.Coordinates;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long restaurantId;
    private String restaurantName;
    private List<FoodItemOrderRequest> items;
    private Coordinates customerCoordinates;
    private Coordinates restaurantCoordinates;
    private Double totalPrice;
    private Double deliveryCharges;
}
