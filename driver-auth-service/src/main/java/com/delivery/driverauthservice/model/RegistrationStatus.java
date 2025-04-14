package com.delivery.driverauthservice.model;

public enum RegistrationStatus {
    PENDING,                // Initial registration started
    PENDING_VERIFICATION,   // Waiting for admin to verify documents and vehicle
    RETRYING,               // Retrying registration with driver management service
    FAILED,                 // Registration failed
    COMPLETED               // Registration complete, driver profile established in management service
}