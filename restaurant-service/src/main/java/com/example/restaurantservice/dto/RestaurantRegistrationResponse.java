package com.example.restaurantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class RestaurantRegistrationResponse {
    private String restaurantName;
    private String email;
    private String ownerName;
    private String nic;
    private String phone;
    private String address;
    private boolean isVerified;

    // Handle nested objects
    private Map<String, Double> location;
    private Map<String, String> bankDetails;

    public Double getLatitude() {
        return location != null && location.containsKey("lat") ? location.get("lat") : null;
    }

    public Double getLongitude() {
        return location != null && location.containsKey("lng") ? location.get("lng") : null;
    }

    public String getBankAccountOwner() {
        return bankDetails != null && bankDetails.containsKey("accountOwner") ? bankDetails.get("accountOwner") : null;
    }

    public String getBankName() {
        return bankName; // Direct field
    }

    public String getBranchName() {
        return branchName; // Direct field
    }

    public String getAccountNumber() {
        return accountNumber; // Direct field
    }

    // Direct fields for bank info (as seen in the JSON response)
    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("branchName")
    private String branchName;

    @JsonProperty("accountNumber")
    private String accountNumber;
}