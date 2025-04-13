package com.delivery.driverservice.service;

import com.delivery.driverservice.dto.*;

import java.util.List;

public interface DriverService {
    // Driver registration and profile management
    DriverDTO registerDriver(DriverRequest request);
    DriverDTO getDriverDetails(Long driverId);

    // Driver status management
    DriverDTO updateDriverStatus(DriverStatusUpdate update);
    void deactivateDriver(Long driverId);
    void reactivateDriver(Long driverId);

    // Driver verification
    DriverDTO updateDriverVerification(Long driverId, boolean isVerified);
    DriverDTO updateDriverVerificationStatus(Long driverId, DriverVerificationUpdate verificationUpdate);
    Boolean isDriverVerified(Long driverId);

    // Driver availability
    Boolean isDriverAvailable(Long driverId);
    List<DriverDTO> getAvailableDrivers();
    List<DriverDTO> getAvailableVerifiedDrivers();

    // Driver location and proximity
    DriverDTO updateDriverLocation(DriverLocationUpdate locationUpdate);
    List<DriverDTO> getNearbyAvailableDrivers(Double lat, Double lng, Double radius);
    List<DriverDTO> getNearbyAvailableDriversByVehicleType(Double lat, Double lng, Double radius, String vehicleType);

    // Order assignment flow
    DriverDTO handleOrderAssignment(OrderAssignmentRequest assignmentRequest);
    DriverDTO handleOrderAcceptance(OrderAcceptanceRequest acceptanceRequest);
    DriverDTO completeOrder(OrderCompletionRequest completionRequest);

    // Rating management
    DriverDTO updateDriverRating(Long driverId, Double newRatingValue);
}