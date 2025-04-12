package com.delivery.driverauthservice.client;

import com.delivery.driverauthservice.dto.DriverDetailsDTO;
import com.delivery.driverauthservice.dto.DriverRegistrationDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "driver-service", url = "${driver.service.url}")
public interface DriverServiceClient {

    @CircuitBreaker(name = "driverServiceCircuitBreaker", fallbackMethod = "getDriverDetailsFallback")
    @GetMapping("/api/drivers/{driverId}")
    DriverDetailsDTO getDriverDetails(@PathVariable Long driverId);

    @CircuitBreaker(name = "driverServiceCircuitBreaker", fallbackMethod = "registerDriverFallback")
    @PostMapping("/api/drivers")
    DriverDetailsDTO registerDriver(@RequestBody DriverRegistrationDTO registrationDTO);

    @CircuitBreaker(name = "driverServiceCircuitBreaker", fallbackMethod = "updateDriverStatusFallback")
    @PatchMapping("/api/drivers/{driverId}/status")
    DriverDetailsDTO updateDriverStatus(@PathVariable Long driverId, @RequestParam String status);

    // Fallback methods
    default DriverDetailsDTO getDriverDetailsFallback(Long driverId, Throwable t) {
        // Log error and return a minimal driver details object
        return DriverDetailsDTO.builder()
                .id(driverId)
                .isActive(false)
                .build();
    }

    default DriverDetailsDTO registerDriverFallback(DriverRegistrationDTO registrationDTO, Throwable t) {
        // Log error and throw an exception or return a minimal object
        throw new RuntimeException("Driver service is currently unavailable. Please try again later.");
    }

    default DriverDetailsDTO updateDriverStatusFallback(Long driverId, String status, Throwable t) {
        // Log error and return a minimal driver details object
        return DriverDetailsDTO.builder()
                .id(driverId)
                .status("UNKNOWN")
                .build();
    }
}
