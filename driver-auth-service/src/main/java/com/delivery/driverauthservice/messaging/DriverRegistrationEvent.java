package com.delivery.driverauthservice.messaging;

import com.delivery.driverauthservice.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRegistrationEvent implements Serializable {
    private Long tempDriverId;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String licenseNumber;

    // Vehicle details
    private String vehicleType;
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    private String licensePlate;
    private String vehicleColor;

    // Document IDs (documentType -> documentId)
    private Map<DocumentType, Long> documentIds;
}