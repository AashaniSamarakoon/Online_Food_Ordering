package com.example.restaurantauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.restaurantauth.model")
@EnableJpaRepositories("com.example.restaurantauth.repository")
@EnableFeignClients
public class RestaurantAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantAuthApplication.class, args);
    }

}
