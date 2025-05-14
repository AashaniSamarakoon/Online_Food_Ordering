package com.delivery.driverservice.client;

import com.delivery.driverservice.dto.OrderDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "order-assignment-service", url = "${order-assignment.service.url}")
public interface OrderAssignmentServiceClient {

    @GetMapping("/api/assignments/driver/{driverId}/active-order")
    OrderDetailsDTO getDriverActiveOrderDetails(@PathVariable Long driverId);
}