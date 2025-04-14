package com.delivery.driverservice.service.impl;

import com.delivery.driverservice.dto.*;
import com.delivery.driverservice.exception.DriverNotFoundException;
import com.delivery.driverservice.model.Driver;
import com.delivery.driverservice.repository.DriverRepository;
import com.delivery.driverservice.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DriverDTO registerDriver(DriverRequest request) {
        log.info("Processing driver registration: {}", request);

        // Check if driver with same driverId already exists
        Optional<Driver> existingDriver = driverRepository.findByDriverId(request.getDriverId());

        if (existingDriver.isPresent()) {
            Driver driver = existingDriver.get();

            // Update basic information
            driver.setUsername(request.getUsername());
            driver.setFirstName(request.getFirstName());
            driver.setLastName(request.getLastName());
            driver.setEmail(request.getEmail());

            if (request.getPhoneNumber() != null) {
                driver.setPhoneNumber(request.getPhoneNumber());
            }

            // Update vehicle information - make sure field names match
            driver.setLicenseNumber(request.getLicenseNumber());
            driver.setVehicleType(request.getVehicleType());
            driver.setVehicleBrand(request.getVehicleBrand());
            driver.setVehicleModel(request.getVehicleModel());
            driver.setVehicleYear(request.getVehicleYear());
            driver.setLicensePlate(request.getLicensePlate());
            driver.setVehicleColor(request.getVehicleColor());

            if (request.getLatitude() != null) {
                driver.setLatitude(request.getLatitude());
            }
            if (request.getLongitude() != null) {
                driver.setLongitude(request.getLongitude());
            }

            log.info("Updated existing driver with ID: {}", driver.getDriverId());
            return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
        }

        // Create a new driver with field names matching the request
        Driver driver = Driver.builder()
                .driverId(request.getDriverId())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .licenseNumber(request.getLicenseNumber())
                .vehicleType(request.getVehicleType())
                .vehicleBrand(request.getVehicleBrand())
                .vehicleModel(request.getVehicleModel())
                .vehicleYear(request.getVehicleYear())
                .licensePlate(request.getLicensePlate())
                .vehicleColor(request.getVehicleColor())
                .status("OFFLINE")
                .rating(0.0)
                .totalTrips(0)
                .latitude(request.getLatitude() != null ? request.getLatitude() : 0.0)
                .longitude(request.getLongitude() != null ? request.getLongitude() : 0.0)
                .isActive(true)
                .isVerified(false)
                .registeredAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .build();

        log.info("Registered new driver with ID: {}", driver.getDriverId());
        Driver savedDriver = driverRepository.save(driver);
        log.info("Driver saved with ID: {}", savedDriver.getId());
        return modelMapper.map(savedDriver, DriverDTO.class);
    }

    @Override
    @Transactional
    public DriverDTO updateDriverStatus(DriverStatusUpdate update) {
        Driver driver = driverRepository.findByDriverId(update.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + update.getDriverId()));

        String oldStatus = driver.getStatus();
        driver.setStatus(update.getStatus());

        if (update.getLatitude() != null) driver.setLatitude(update.getLatitude());
        if (update.getLongitude() != null) driver.setLongitude(update.getLongitude());

        // Update the last active timestamp
        driver.setLastActiveAt(update.getLastActiveAt() != null ?
                update.getLastActiveAt() : LocalDateTime.now());

        // Log status changes for monitoring
        if (!oldStatus.equals(update.getStatus())) {
            log.info("Driver {} status changed from {} to {}",
                    driver.getDriverId(), oldStatus, update.getStatus());
        }

        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Transactional
    @Override
    public DriverDTO handleOrderAssignment(OrderAssignmentRequest assignmentRequest) {
        Driver driver = driverRepository.findByDriverId(assignmentRequest.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + assignmentRequest.getDriverId()));

        // Check if driver is available before assigning
        if (!"AVAILABLE".equals(driver.getStatus())) {
            log.warn("Attempted to assign order to unavailable driver {}. Current status: {}",
                    driver.getDriverId(), driver.getStatus());
            throw new IllegalStateException("Driver is not available for assignment");
        }

        // Change driver status to assigned/busy
        driver.setStatus("ASSIGNED");
        driver.setLastActiveAt(LocalDateTime.now());

        // Here you could store the current order ID if needed
        // driver.setCurrentOrderId(assignmentRequest.getOrderId());

        log.info("Driver {} assigned to order {}", driver.getDriverId(), assignmentRequest.getOrderId());
        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Transactional
    @Override
    public DriverDTO handleOrderAcceptance(OrderAcceptanceRequest acceptanceRequest) {
        Driver driver = driverRepository.findByDriverId(acceptanceRequest.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + acceptanceRequest.getDriverId()));

        if ("ASSIGNED".equals(driver.getStatus())) {
            // If driver accepts, update status to "BUSY"
            if (acceptanceRequest.isAccepted()) {
                driver.setStatus("BUSY");
                log.info("Driver {} accepted order {}", driver.getDriverId(), acceptanceRequest.getOrderId());
            } else {
                // If driver rejects, update status back to "AVAILABLE"
                driver.setStatus("AVAILABLE");
                log.info("Driver {} rejected order {}", driver.getDriverId(), acceptanceRequest.getOrderId());
            }

            driver.setLastActiveAt(LocalDateTime.now());
            return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
        } else {
            log.warn("Invalid order acceptance state for driver {}. Current status: {}",
                    driver.getDriverId(), driver.getStatus());
            throw new IllegalStateException("Driver is not in ASSIGNED state");
        }
    }

    @Transactional
    @Override
    public DriverDTO completeOrder(OrderCompletionRequest completionRequest) {
        Driver driver = driverRepository.findByDriverId(completionRequest.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + completionRequest.getDriverId()));

        if (!"BUSY".equals(driver.getStatus())) {
            log.warn("Driver {} attempted to complete order but status is {}",
                    driver.getDriverId(), driver.getStatus());
            throw new IllegalStateException("Driver is not in BUSY state");
        }

        // Reset status to AVAILABLE
        driver.setStatus("AVAILABLE");
        driver.setLastActiveAt(LocalDateTime.now());

        // Increment trip count
        driver.setTotalTrips(driver.getTotalTrips() + 1);

        log.info("Driver {} completed order {}", driver.getDriverId(), completionRequest.getOrderId());
        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Transactional
    @Override
    public DriverDTO updateDriverLocation(DriverLocationUpdate locationUpdate) {
        Driver driver = driverRepository.findByDriverId(locationUpdate.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + locationUpdate.getDriverId()));

        // Update location
        driver.setLatitude(locationUpdate.getLatitude());
        driver.setLongitude(locationUpdate.getLongitude());
        driver.setLastActiveAt(LocalDateTime.now());

        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Override
    public DriverDTO updateDriverVerificationStatus(Long driverId, DriverVerificationUpdate verificationUpdate) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));

        // Update verification status
        if (verificationUpdate.isPhoneVerified()) {
            log.info("Phone verified for driver {}", driverId);
        }

        if (verificationUpdate.isDocumentsVerified()) {
            driver.setIsVerified(true);
            log.info("Documents verified for driver {}", driverId);
        }

        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Override
    public List<DriverDTO> getAvailableDrivers() {
        return driverRepository.findByStatusAndIsActiveTrue("AVAILABLE").stream()
                .map(driver -> modelMapper.map(driver, DriverDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverDTO> getAvailableVerifiedDrivers() {
        return driverRepository.findVerifiedAvailableDrivers().stream()
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
    public List<DriverDTO> getNearbyAvailableDriversByVehicleType(Double lat, Double lng, Double radius, String vehicleType) {
        return driverRepository.findNearbyAvailableDriversByVehicleType(lat, lng, radius, vehicleType).stream()
                .map(driver -> modelMapper.map(driver, DriverDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public DriverDTO getDriverDetails(Long driverId) {
        return driverRepository.findByDriverId(driverId)
                .map(driver -> modelMapper.map(driver, DriverDTO.class))
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));
    }

    @Override
    @Transactional
    public void deactivateDriver(Long driverId) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));
        driver.setIsActive(false);
        driver.setStatus("OFFLINE");
        driverRepository.save(driver);
        log.info("Driver {} deactivated", driverId);
    }

    @Transactional
    @Override
    public void reactivateDriver(Long driverId) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));
        driver.setIsActive(true);
        driver.setStatus("OFFLINE"); // Start as offline until they explicitly go online
        driverRepository.save(driver);
        log.info("Driver {} reactivated", driverId);
    }

    @Override
    public Boolean isDriverAvailable(Long driverId) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));
        return "AVAILABLE".equals(driver.getStatus()) && driver.getIsActive();
    }

    @Override
    public Boolean isDriverVerified(Long driverId) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));
        return driver.getIsVerified();
    }

    @Override
    @Transactional
    public DriverDTO updateDriverVerification(Long driverId, boolean isVerified) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));
        driver.setIsVerified(isVerified);
        log.info("Driver {} verification status updated to {}", driverId, isVerified);
        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }

    @Override
    @Transactional
    public DriverDTO updateDriverRating(Long driverId, Double newRatingValue) {
        Driver driver = driverRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));

        // Update the driver's rating and increment total trips
        Double currentRating = driver.getRating();
        int totalTrips = driver.getTotalTrips();

        // Calculate new average rating
        double updatedRating;
        if (totalTrips == 0) {
            updatedRating = newRatingValue;
        } else {
            updatedRating = ((currentRating * totalTrips) + newRatingValue) / (totalTrips + 1);
        }

        driver.setRating(updatedRating);
        log.info("Driver {} rating updated to {}", driverId, updatedRating);

        return modelMapper.map(driverRepository.save(driver), DriverDTO.class);
    }
}