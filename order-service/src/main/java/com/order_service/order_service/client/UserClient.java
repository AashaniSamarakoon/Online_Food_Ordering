package com.order_service.order_service.client;

import com.order_service.order_service.model.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", url = "http://localhost:8090")
public interface UserClient {
    @GetMapping("/auth/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @GetMapping("/auth/profile")
    Map<String, Object> getUserProfile(@RequestHeader("Authorization") String token);
}

