package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {
    private String orderId;
    private String driverId;
    private String status;
    private Map<String, Object> metadata;
}