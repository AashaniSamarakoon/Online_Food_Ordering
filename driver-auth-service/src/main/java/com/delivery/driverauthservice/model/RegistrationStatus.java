package com.delivery.driverauthservice.model;

public enum RegistrationStatus {
    PENDING,      // Not yet synced with driver service
    COMPLETED,    // Successfully synced
    FAILED        // Sync attempted but failed
}