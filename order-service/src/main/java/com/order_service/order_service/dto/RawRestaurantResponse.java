package com.order_service.order_service.dto;

import com.order_service.order_service.model.Coordinates;
import lombok.Data;

import java.util.List;

@Data
public class RawRestaurantResponse {
    private RestaurantDetails restaurantDetails;
    private List<FoodItemResponse> menuItems;
}