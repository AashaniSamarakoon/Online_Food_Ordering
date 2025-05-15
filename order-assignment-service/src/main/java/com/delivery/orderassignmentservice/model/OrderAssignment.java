package com.delivery.orderassignmentservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "order_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime expiryTime;

    @ElementCollection
    @CollectionTable(name = "order_assignment_candidates", joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "driver_id")
    private List<Long> candidateDrivers;
}