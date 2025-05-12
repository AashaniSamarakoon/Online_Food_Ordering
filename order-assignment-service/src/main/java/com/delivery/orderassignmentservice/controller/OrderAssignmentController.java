package com.delivery.orderassignmentservice.controller;

import com.delivery.orderassignmentservice.dto.AssignmentRequest;
import com.delivery.orderassignmentservice.dto.AssignmentStatusUpdate;
import com.delivery.orderassignmentservice.dto.OrderAssignmentDTO;
import com.delivery.orderassignmentservice.dto.OrderDetailsDTO;
import com.delivery.orderassignmentservice.dto.notification.OrderAssignmentNotification;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class OrderAssignmentController {

    private final OrderAssignmentService assignmentService;

    @PostMapping("/process-order/{orderId}")
    public ResponseEntity<OrderAssignmentDTO> processOrderAssignment(@PathVariable Long orderId) {
        OrderAssignmentDTO assignment = assignmentService.processOrderAssignment(orderId);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

    @PatchMapping("/status")
    public ResponseEntity<OrderAssignmentDTO> updateStatus(@Valid @RequestBody AssignmentStatusUpdate statusUpdate) {
        OrderAssignmentDTO updated = assignmentService.updateAssignmentStatus(statusUpdate);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderAssignmentDTO>> getByOrder(@PathVariable Long orderId) {
        List<OrderAssignmentDTO> assignments = assignmentService.getAssignmentsByOrder(orderId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<OrderAssignmentDTO>> getByDriver(@PathVariable Long driverId) {
        List<OrderAssignmentDTO> assignments = assignmentService.getAssignmentsByDriver(driverId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/driver/{driverId}/active-order")
    public ResponseEntity<OrderDetailsDTO> getDriverActiveOrderDetails(@PathVariable Long driverId) {
        return ResponseEntity.ok(assignmentService.getDriverActiveOrderDetails(driverId));
    }

    @GetMapping("/{driverId}/pending-assignments")
    public ResponseEntity<List<OrderAssignmentNotification>> getPendingAssignments(
            @PathVariable Long driverId) {

        List<OrderAssignmentNotification> pendingAssignments =
                assignmentService.getPendingAssignmentsForDriver(driverId);

        return ResponseEntity.ok(pendingAssignments);
    }

    @PutMapping("/{orderId}/confirm/{driverId}")
    public ResponseEntity<Void> confirmAssignment(
            @PathVariable Long orderId,
            @PathVariable Long driverId) {
        assignmentService.confirmAssignment(orderId, driverId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/reject/{driverId}")
    public ResponseEntity<Void> rejectAssignment(
            @PathVariable Long orderId,
            @PathVariable Long driverId) {
        assignmentService.handleRejection(orderId, driverId);
        return ResponseEntity.ok().build();
    }
}