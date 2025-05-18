//package com.delivery.orderassignmentservice.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class OrderDetailsDTO {
//    private Long id;
//    private String orderNumber;
//    private String status;
//    private LocalDateTime createdAt;
//    private LocalDateTime assignmentTime;
//
//    // Customer details (limited for privacy)
//    private String customerName;
//    private String customerPhone;
//
//    // Location details
//    private String pickupAddress;
//    private String deliveryAddress;
//
//    // Coordinates using embedded object pattern
//    private LocationDTO restaurantCoordinates;
//    private LocationDTO customerCoordinates;
//
//    // Order details
//    private List<OrderItemDTO> items;
//    private Double totalPrice;
//    private Double deliveryCharges;
//    private BigDecimal total;
//    private String specialInstructions;
//    private boolean contactlessDelivery;
//
////     Restaurant details
//    private String restaurantName;
//    private String restaurantPhone;
//
////     Payment information
//    private String paymentMethod;
//    private String paymentStatus;
//    private String transactionId;
//
////     Timing information
//    private LocalDateTime orderTime;
//    private LocalDateTime deliveryTimeEstimate;
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class OrderItemDTO {
//        private String name;
//        private Integer quantity;
//        private String specialInstructions;
//    }
//
//
//}

package com.delivery.orderassignmentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)  // Important to handle any extra fields from response
public class OrderDetailsDTO {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String status;

    // Coordinates
    private LocationDTO restaurantCoordinates;
    private LocationDTO customerCoordinates;

    // Order details
    private List<OrderItemDTO> items;
    private Double totalPrice;
    private Double deliveryCharges;

    // These can be set after retrieval if needed by your business logic
    private LocalDateTime createdAt;
    private LocalDateTime assignmentTime;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemDTO {
        private Long foodItemId;
        private String name;
        private String description;
        private Double price;
        private Integer quantity;
        private Double subtotal;
        private Boolean available;
    }
}