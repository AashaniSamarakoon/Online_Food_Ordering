package com.delivery.orderassignmentservice.dto.events;

import com.delivery.orderassignmentservice.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private String orderNumber;
    private LocationDTO restaurantCoordinates;
    private LocationDTO customerCoordinates;
    private String restaurantName;
    private String restaurantAddress;
    private String customerAddress;
}