package com.delivery.driverauthservice.messaging;

import com.delivery.driverauthservice.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegistrationEvent implements Serializable {
    private Long tempDriverId;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String vehicleType;
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    private String licensePlate;
    private String vehicleColor;
    private Map<DocumentType, Long> documentIds;
    private LocalDateTime retryTimestamp;
}