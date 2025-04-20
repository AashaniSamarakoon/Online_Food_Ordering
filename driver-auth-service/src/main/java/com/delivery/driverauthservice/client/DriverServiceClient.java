package com.delivery.driverauthservice.client;

import com.delivery.driverauthservice.dto.DriverDetailsDTO;
import com.delivery.driverauthservice.dto.DriverRegistrationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "driver-service", url = "${driver-service.url}")
public interface DriverServiceClient {

    /**
     * Registers a new driver with the driver management service.
     * This is only called during the admin verification process after
     * documents and vehicle information have been verified.
     */
    @PostMapping("/api/drivers")
    DriverDetailsDTO registerDriver(@RequestBody DriverRegistrationDTO request);
}