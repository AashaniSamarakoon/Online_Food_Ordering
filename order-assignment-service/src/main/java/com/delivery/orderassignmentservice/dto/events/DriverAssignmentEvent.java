package com.delivery.orderassignmentservice.dto.events;

import com.delivery.orderassignmentservice.dto.DriverLocationDTO;
import com.delivery.orderassignmentservice.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DriverAssignmentEvent {
    private Long orderId;
    private Long driverId;
    private LocationDTO restaurantLocation;
    private LocalDateTime expiryTime;


}