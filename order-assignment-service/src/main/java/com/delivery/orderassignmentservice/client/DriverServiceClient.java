package com.delivery.orderassignmentservice.client;

import com.delivery.orderassignmentservice.dto.DriverDTO;
import com.delivery.orderassignmentservice.dto.DriverStatusUpdate;
import com.delivery.orderassignmentservice.dto.OrderAssignmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "driver-service", url = "${driver.service.url}")
public interface DriverServiceClient {

    @GetMapping("/api/drivers/{driverId}")
    DriverDTO getDriverDetails(@PathVariable Long driverId);

    @PatchMapping("/api/drivers/{driverId}/status")
    void updateDriverStatus(
            @PathVariable Long driverId,
            @RequestBody DriverStatusUpdate update);
}