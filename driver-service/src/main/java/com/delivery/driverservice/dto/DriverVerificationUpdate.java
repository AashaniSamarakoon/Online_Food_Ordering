package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverVerificationUpdate {
    private boolean phoneVerified;
    private boolean documentsVerified;
    private String verificationNotes;
}