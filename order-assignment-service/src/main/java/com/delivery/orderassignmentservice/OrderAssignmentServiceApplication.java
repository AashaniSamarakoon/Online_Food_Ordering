package com.delivery.orderassignmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients

public class OrderAssignmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderAssignmentServiceApplication.class, args);
    }
//
}

