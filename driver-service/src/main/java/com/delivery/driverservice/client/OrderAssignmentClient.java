package com.delivery.driverservice.client;

import com.delivery.driverservice.dto.OrderAssignmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-assignment-service", url = "${order.service.url}")
public interface OrderAssignmentClient {

    @GetMapping("/api/orders/driver/{driverId}")
    List<OrderAssignmentDTO> getActiveDriverAssignments(@PathVariable Long driverId);


}

//