package com.delivery.driverservice.service.impl;


import com.delivery.driverservice.dto.*;
import com.delivery.driverservice.exception.DriverNotFoundException;
import com.delivery.driverservice.model.Driver;
import com.delivery.driverservice.repository.DriverRepository;
import com.delivery.driverservice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DriverDTO registerDriver(DriverRequest request) {
        Driver driver = modelMapper.map(request, Driver.class);
        driver.setStatus("AVAILABLE");
        driver.setRating(0.0); // Default rating
        driver.setIsActive(true);
        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Override
    @Transactional
    public DriverDTO updateDriverStatus(DriverStatusUpdate update) {
        Driver driver = driverRepository.findById(update.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found"));

        driver.setStatus(update.getStatus());
        if (update.getLatitude() != null) driver.setLatitude(update.getLatitude());
        if (update.getLongitude() != null) driver.setLongitude(update.getLongitude());

        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Override
    public List<DriverDTO> getAvailableDrivers() {
        return driverRepository.findAvailableDrivers().stream()
                .map(driver -> modelMapper.map(driver, DriverDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverDTO> getNearbyAvailableDrivers(Double lat, Double lng, Double radius) {
        return driverRepository.findNearbyAvailableDrivers(lat, lng, radius).stream()
                .map(driver -> modelMapper.map(driver, DriverDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public DriverDTO getDriverDetails(Long driverId) {
        return driverRepository.findById(driverId)
                .map(driver -> modelMapper.map(driver, DriverDTO.class))
                .orElseThrow(() -> new DriverNotFoundException("Driver not found"));
    }

    @Override
    @Transactional
    public void deactivateDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found"));
        driver.setIsActive(false);
        driverRepository.save(driver);
    }

    @Override
    public Boolean isDriverAvailable(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found"));
        return "AVAILABLE".equals(driver.getStatus());
    }
}