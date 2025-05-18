package com.delivery.orderassignmentservice.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(10000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
    }
}