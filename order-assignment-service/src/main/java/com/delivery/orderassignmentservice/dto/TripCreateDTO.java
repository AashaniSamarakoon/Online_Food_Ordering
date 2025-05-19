package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripCreateDTO {
    private String orderId;
    private String customerId;
    private List<TripWaypointDTO> waypoints;
    private double estimatedDistance;
    private int estimatedDuration;
    private Object mapApiData; // Use Object to handle the complex structure
}