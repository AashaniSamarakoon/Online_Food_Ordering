package com.delivery.orderassignmentservice.repository;

import com.delivery.orderassignmentservice.model.AssignmentCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentCandidateRepository extends JpaRepository<AssignmentCandidate, Long> {

    @Query(value = "SELECT COUNT(*) > 0 FROM order_assignment_candidates " +
            "WHERE assignment_id = :assignmentId AND driver_id = :driverId", nativeQuery = true)
    Boolean existsByAssignmentIdAndDriverId(@Param("assignmentId") Long assignmentId,
                                            @Param("driverId") Long driverId);
}