package com.delivery.driverauthservice.controller;

import com.delivery.driverauthservice.dto.VehicleDTO;
import com.delivery.driverauthservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<VehicleDTO> createVehicle(@RequestBody VehicleDTO vehicleDTO) {
        VehicleDTO createdVehicle = vehicleService.createVehicle(vehicleDTO);
        return ResponseEntity.ok(createdVehicle);
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<VehicleDTO> getVehicleByDriverId(@PathVariable Long driverId) {
        VehicleDTO vehicle = vehicleService.getVehicleByDriverId(driverId);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<VehicleDTO> updateVehicle(
            @PathVariable Long driverId,
            @RequestBody VehicleDTO vehicleDTO) {

        VehicleDTO updatedVehicle = vehicleService.updateVehicle(driverId, vehicleDTO);
        return ResponseEntity.ok(updatedVehicle);
    }

    @PutMapping("/{vehicleId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> verifyVehicle(
            @PathVariable Long vehicleId,
            @RequestParam boolean verified) {

        boolean result = vehicleService.verifyVehicle(vehicleId, verified);
        return ResponseEntity.ok(result);
    }
}