package com.delivery.driverauthservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSyncResultEvent implements Serializable {
    private Long tempDriverId;
    private Long actualDriverId;
    private boolean success;
    private String errorMessage;
}