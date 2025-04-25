package com.delivery.orderassignmentservice.client;

import com.delivery.orderassignmentservice.dto.DriverDTO;
import com.delivery.orderassignmentservice.dto.OrderAssignmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "driver-service", url = "${driver.service.url}")
public interface DriverServiceClient {

    @GetMapping("/api/drivers/{driverId}")
    DriverDTO getDriverDetails(@PathVariable Long driverId);

    @GetMapping("/api/drivers/{driverId}/available")
    Boolean checkDriverAvailability(@PathVariable Long driverId);

    @PutMapping("/api/drivers/{driverId}/assign")
    void assignOrderToDriver(
            @PathVariable Long driverId,
            @RequestBody OrderAssignmentDTO assignment
    );
}