package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAssignmentDTO {
    private Long id;
    private Long orderId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}