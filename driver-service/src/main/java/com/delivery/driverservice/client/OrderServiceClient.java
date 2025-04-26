package com.delivery.driverservice.client;

import com.delivery.driverservice.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "order-assignment-service", url = "${order.service.url}")
public interface OrderServiceClient {

    @GetMapping("/api/orders/driver/{driverId}")
    List<OrderDTO> getDriverOrders(@PathVariable Long driverId);


//    @PutMapping("/api/orders/{orderId}/assign/{driverId}")
//    void assignDriverToOrder(@PathVariable Long orderId, @PathVariable Long driverId);
}
//
//