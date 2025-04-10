package com.delivery.orderassignmentservice.repository;

import com.delivery.orderassignmentservice.model.OrderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment, Long> {
    List<OrderAssignment> findByOrderId(Long orderId);
    List<OrderAssignment> findByDriverId(Long driverId);
    List<OrderAssignment> findByStatus(String status);
}