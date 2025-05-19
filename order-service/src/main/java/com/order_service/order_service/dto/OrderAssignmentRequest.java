package com.order_service.order_service.dto;

import com.order_service.order_service.model.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAssignmentRequest {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;

    private Coordinates customerCoordinates;
    private Coordinates restaurantCoordinates;
}
