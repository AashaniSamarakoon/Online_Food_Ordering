package com.delivery.orderassignmentservice.controller;

import com.delivery.orderassignmentservice.dto.AssignmentRequest;
import com.delivery.orderassignmentservice.dto.AssignmentStatusUpdate;
import com.delivery.orderassignmentservice.dto.OrderAssignmentDTO;
import com.delivery.orderassignmentservice.dto.OrderDetailsDTO;
import com.delivery.orderassignmentservice.dto.notification.OrderAssignmentNotification;
import com.delivery.orderassignmentservice.exception.AssignmentNotFoundException;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
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
        try {
            log.info("Received confirmation request for order {} by driver {}", orderId, driverId);

            // Add locking mechanism to prevent race condition with rejection
            synchronized (this.getClass()) {
                // First check if this order was recently rejected before proceeding
                if (recentlyRejectedOrders.getIfPresent(orderId + ":" + driverId) != null) {
                    log.warn("Order {} was recently rejected by driver {}, ignoring confirmation", orderId, driverId);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(null);
                }

                assignmentService.confirmAssignment(orderId, driverId);

                // Add this to a completed orders cache to prevent future rejections
                confirmedOrders.put(orderId + ":" + driverId, Boolean.TRUE);
            }

            return ResponseEntity.ok().build();
        } catch (AssignmentNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error confirming assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}/reject/{driverId}")
    public ResponseEntity<Void> rejectAssignment(
            @PathVariable Long orderId,
            @PathVariable Long driverId) {
        try {
            log.info("Received rejection request for order {} by driver {}", orderId, driverId);

            // Add locking mechanism to prevent race condition with confirmation
            synchronized (this.getClass()) {
                // First check if this order was recently confirmed before proceeding
                if (confirmedOrders.getIfPresent(orderId + ":" + driverId) != null) {
                    log.warn("Order {} was already confirmed by driver {}, ignoring rejection", orderId, driverId);
                    return ResponseEntity.ok().build();
                }

                assignmentService.handleRejection(orderId, driverId);

                // Add this to a rejected orders cache
                recentlyRejectedOrders.put(orderId + ":" + driverId, Boolean.TRUE);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error rejecting assignment: {}", e.getMessage(), e);
            // Still return OK to prevent client errors
            return ResponseEntity.ok().build();
        }
    }

    // Add these fields to the controller class
    private final Cache<String, Boolean> confirmedOrders = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Boolean> recentlyRejectedOrders = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
}