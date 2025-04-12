package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.dto.VehicleDTO;

public interface VehicleService {
    VehicleDTO createVehicle(VehicleDTO vehicleDTO);
    VehicleDTO getVehicleByDriverId(Long driverId);
    VehicleDTO updateVehicle(Long driverId, VehicleDTO vehicleDTO);
    boolean verifyVehicle(Long vehicleId, boolean verified);
}