package com.delivery.orderassignmentservice.controller;

import com.delivery.orderassignmentservice.dto.*;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class OrderAssignmentController {

    private final OrderAssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<OrderAssignmentDTO> createAssignment(
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.createAssignment(request));
    }

    @PatchMapping("/status")
    public ResponseEntity<OrderAssignmentDTO> updateAssignmentStatus(
            @Valid @RequestBody AssignmentStatusUpdate update) {
        return ResponseEntity.ok(assignmentService.updateAssignmentStatus(update));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderAssignmentDTO>> getAssignmentsByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByOrder(orderId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<OrderAssignmentDTO>> getAssignmentsByDriver(
            @PathVariable Long driverId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByDriver(driverId));

    }
}