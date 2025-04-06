package com.delivery.driverservice.controller;

import com.delivery.driverservice.dto.*;
import com.delivery.driverservice.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverDTO> registerDriver(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.ok(driverService.registerDriver(request));
    }

    @PatchMapping("/status")
    public ResponseEntity<DriverDTO> updateDriverStatus(@Valid @RequestBody DriverStatusUpdate update) {
        return ResponseEntity.ok(driverService.updateDriverStatus(update));
    }

    @GetMapping("/available")
    public ResponseEntity<List<DriverDTO>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DriverDTO>> getNearbyAvailableDrivers(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5.0") Double radius) {
        return ResponseEntity.ok(driverService.getNearbyAvailableDrivers(lat, lng, radius));
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverDTO> getDriverDetails(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getDriverDetails(driverId));
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deactivateDriver(@PathVariable Long driverId) {
        driverService.deactivateDriver(driverId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/{driverId}/available")
    public ResponseEntity<Boolean> checkDriverAvailability(@PathVariable Long driverId) {
        boolean isAvailable = driverService.isDriverAvailable(driverId);
        return ResponseEntity.ok(isAvailable);
    }
}