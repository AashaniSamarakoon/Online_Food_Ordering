package com.delivery.driverauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients

public class DriverAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriverAuthServiceApplication.class, args);
    }

}
