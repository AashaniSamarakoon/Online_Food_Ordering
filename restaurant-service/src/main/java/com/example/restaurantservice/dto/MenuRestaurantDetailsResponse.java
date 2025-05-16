package com.example.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRestaurantDetailsResponse {
    private RestaurantResponse restaurantDetails;
    private List<MenuItemResponse> menuItems;

    // Additional restaurant statistics
//    private Integer totalMenuItems;
//    private Double averagePrice;
//    private Integer availableItems;
//    private Integer outOfStockItems;
}