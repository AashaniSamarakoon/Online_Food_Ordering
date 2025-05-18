//package com.example.restaurantservice.dto;
//
//import lombok.Data;
//import java.math.BigDecimal;
//
//@Data
//public class OrderItemDTO {
//    private Long foodItemId;
//    private String name;
//    private String description;
//    private String category;
//    private BigDecimal price;
//    private Integer quantity;
//    private BigDecimal subtotal;
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
public class OrderItemDTO {
    private Long foodItemId;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}