//package com.example.restaurantservice.dto;
//
//import lombok.Data;
//import java.math.BigDecimal;
//import java.util.List;
//
//@Data
//public class OrderDTO {
//    private Long id;
//    private Long restaurantId;
//    private String restaurantName;
//    private String username;
//    private String email;
//    private String phoneNumber;
//    private BigDecimal totalPrice;
//    private String status;
//    private List<OrderItemDTO> items;
//    private String address;
//    private Double customerLatitude;
//    private Double customerLongitude;
//}

package com.example.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private List<OrderItemDTO> items;
    private Double totalPrice;
    private Double deliveryCharges;
    private String status;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private CoordinatesDTO restaurantCoordinates;
    private CoordinatesDTO customerCoordinates;
    private LocalDateTime createdAt;
}