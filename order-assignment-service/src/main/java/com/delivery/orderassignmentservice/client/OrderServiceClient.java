package com.delivery.orderassignmentservice.client;

import com.delivery.orderassignmentservice.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{orderId}")
    OrderDTO getOrderDetails(@PathVariable Long orderId);

    @PatchMapping("/api/orders/{orderId}/status")
    void updateOrderStatus(
            @PathVariable Long orderId
//            @RequestBody OrderStatusUpdate statusUpdate
    );
}
