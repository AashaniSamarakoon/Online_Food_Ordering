package com.order_service.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "order-assignment-service", url = "http://localhost:8088")
public interface OrderAssignmentClient {

    @PostMapping("/api/assignments/process-order/{orderId}")
    void processOrderAssignment(@PathVariable("orderId") Long orderId);
}


