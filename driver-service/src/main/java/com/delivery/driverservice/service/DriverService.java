package com.delivery.driverservice.service;



import com.delivery.driverservice.dto.*;

import java.util.List;

public interface DriverService {
    DriverDTO registerDriver(DriverRequest request);
    DriverDTO updateDriverStatus(DriverStatusUpdate update);
    List<DriverDTO> getAvailableDrivers();
    List<DriverDTO> getNearbyAvailableDrivers(Double lat, Double lng, Double radius);
    DriverDTO getDriverDetails(Long driverId);
    void deactivateDriver(Long driverId);

    Boolean isDriverAvailable(Long driverId);

}