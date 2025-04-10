package com.delivery.orderassignmentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentStatusUpdate {
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    @NotNull(message = "Status is required")
    private String status; // ACCEPTED, REJECTED
}