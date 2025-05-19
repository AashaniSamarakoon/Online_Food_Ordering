package com.delivery.orderassignmentservice.repository;

import com.delivery.orderassignmentservice.model.OrderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment, Long> {
    List<OrderAssignment> findByOrderId(Long orderId);
    List<OrderAssignment> findByDriverId(Long driverId);
    List<OrderAssignment> findByStatus(String status);

    // Custom query for counting rejections
    @Query("SELECT COUNT(a) FROM OrderAssignment a WHERE a.orderId = :orderId AND a.status = 'REJECTED'")
    int countRejectionsByOrder(@Param("orderId") Long orderId);

    // Count by orderId and status
    long countByOrderIdAndStatus(Long orderId, String status);

    // Check if assignment exists with specified criteria
    boolean existsByOrderIdAndDriverIdAndStatus(Long orderId, Long driverId, String status);

    // Find by orderId and status
    Optional<OrderAssignment> findByOrderIdAndStatus(Long orderId, String status);

    // Find by orderId and driverId
    Optional<OrderAssignment> findByOrderIdAndDriverId(Long orderId, Long driverId);

    Optional<OrderAssignment> findByDriverIdAndStatus(Long driverId, String status);
    List<OrderAssignment> findAllByDriverIdAndStatus(Long driverId, String status);

    List<OrderAssignment> findByOrderIdAndStatusAndIdNot(Long orderId, String status, Long excludedId);

}