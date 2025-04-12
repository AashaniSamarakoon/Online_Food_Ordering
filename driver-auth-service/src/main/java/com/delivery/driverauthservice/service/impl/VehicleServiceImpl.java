package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.dto.VehicleDTO;
import com.delivery.driverauthservice.exception.ResourceAlreadyExistsException;
import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.model.Vehicle;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import com.delivery.driverauthservice.repository.VehicleRepository;
import com.delivery.driverauthservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverCredentialRepository driverRepository;

    @Override
    @Transactional
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) {
        // Check if license plate already exists
        if (vehicleRepository.existsByLicensePlate(vehicleDTO.getLicensePlate())) {
            throw new ResourceAlreadyExistsException("Vehicle with this license plate already exists");
        }

        DriverCredential driver = driverRepository.findById(vehicleDTO.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check if driver already has a vehicle
        if (vehicleRepository.findByDriverDriverId(vehicleDTO.getDriverId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Driver already has a registered vehicle");
        }

        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .brand(vehicleDTO.getBrand())
                .model(vehicleDTO.getModel())
                .year(vehicleDTO.getYear())
                .licensePlate(vehicleDTO.getLicensePlate())
                .color(vehicleDTO.getColor())
                .vehicleType(vehicleDTO.getVehicleType())
                .verified(false)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        return mapToDTO(vehicle);
    }

    @Override
    public VehicleDTO getVehicleByDriverId(Long driverId) {
        return vehicleRepository.findByDriverDriverId(driverId)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public VehicleDTO updateVehicle(Long driverId, VehicleDTO vehicleDTO) {
        return vehicleRepository.findByDriverDriverId(driverId)
                .map(vehicle -> {
                    // Check if license plate is changed and already exists
                    if (!vehicle.getLicensePlate().equals(vehicleDTO.getLicensePlate()) &&
                            vehicleRepository.existsByLicensePlate(vehicleDTO.getLicensePlate())) {
                        throw new ResourceAlreadyExistsException("Vehicle with this license plate already exists");
                    }

                    vehicle.setBrand(vehicleDTO.getBrand());
                    vehicle.setModel(vehicleDTO.getModel());
                    vehicle.setYear(vehicleDTO.getYear());
                    vehicle.setLicensePlate(vehicleDTO.getLicensePlate());
                    vehicle.setColor(vehicleDTO.getColor());
                    vehicle.setVehicleType(vehicleDTO.getVehicleType());
                    vehicle.setVerified(false); // Reset verification on update

                    return mapToDTO(vehicleRepository.save(vehicle));
                })
                .orElseGet(() -> createVehicle(vehicleDTO));
    }

    @Override
    @Transactional
    public boolean verifyVehicle(Long vehicleId, boolean verified) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.setVerified(verified);
                    vehicleRepository.save(vehicle);
                    return true;
                })
                .orElse(false);
    }

    private VehicleDTO mapToDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriver().getDriverId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .verified(vehicle.getVerified())
                .build();
    }
}