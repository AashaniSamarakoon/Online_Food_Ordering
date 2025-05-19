package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripWaypointDTO {
    private String type;
    private double longitude;
    private double latitude;
    private String address;
}