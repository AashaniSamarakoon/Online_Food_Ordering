package com.delivery.driverservice.service;

import com.delivery.driverservice.dto.*;

import java.util.List;

public interface DriverService {
    // Driver registration and profile management
    DriverDTO registerDriver(DriverRequest request);
    DriverDTO getDriverDetails(Long driverId);

    // Driver status management
    DriverDTO updateDriverStatus(DriverStatusUpdate update);

    // Driver verification
    DriverDTO updateDriverVerificationStatus(Long driverId, DriverVerificationUpdate verificationUpdate);
    Boolean isDriverVerified(Long driverId);

    // Driver availability
    Boolean isDriverAvailable(Long driverId);
    List<DriverDTO> getAvailableDrivers();

    // Driver location and proximity
    DriverDTO updateDriverLocation(DriverLocationUpdate locationUpdate);

    // Order assignment flow
//    DriverDTO completeOrder(OrderCompletionRequest completionRequest);

    // Rating management
    DriverDTO updateDriverRating(Long driverId, Double newRatingValue);
}