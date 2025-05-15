package com.restaurant_service.restaurant_service.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
public class RestaurantCoordinates {
    private double latitude;
    private double longitude;

    // Constructors
    public RestaurantCoordinates() {}

    public RestaurantCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

