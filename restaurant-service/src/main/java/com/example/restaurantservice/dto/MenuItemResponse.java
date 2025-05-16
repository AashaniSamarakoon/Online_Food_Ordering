//package com.example.restaurantservice.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class MenuItemResponse {
//    private Long id;
//    private String name;
//    private String description;
//    private Double price;
//    private String category;
//    private Boolean available;
//}

package com.example.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private String status; // Changed from Boolean available
    private String description;
    private String imageUrl; // Added image URL
}