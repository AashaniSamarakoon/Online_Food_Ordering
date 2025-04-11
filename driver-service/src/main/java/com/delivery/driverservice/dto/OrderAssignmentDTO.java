package com.delivery.orderassignmentservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAssignmentDTO {
    private Long id;
    private Long orderId;
    private AssignDriverDTO driver;  // Changed from driverId to full driver object
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class AssignDriverDTO {
        private Long id;
        private String name;
        private String vehicleType;
        private Double rating;
    }
}