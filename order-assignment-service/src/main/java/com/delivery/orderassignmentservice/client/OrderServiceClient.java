package com.delivery.orderassignmentservice.client;

import com.delivery.orderassignmentservice.config.FeignClientConfig;
import com.delivery.orderassignmentservice.dto.OrderDetailsDTO;
import com.delivery.orderassignmentservice.dto.OrderStatusUpdate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "${order.service.url}", configuration = FeignClientConfig.class)
public interface OrderServiceClient {

    @GetMapping("/orders/public/{orderId}")
    OrderDetailsDTO getOrderDetails(@PathVariable Long orderId);

    @PatchMapping("/orders/{orderId}/status")
    void updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdate statusUpdate
    );
}
