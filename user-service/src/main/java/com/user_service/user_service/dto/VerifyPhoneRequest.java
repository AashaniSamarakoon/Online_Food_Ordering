// VerifyPhoneRequest.java
package com.user_service.user_service.dto;

import lombok.Data;

@Data
public class VerifyPhoneRequest {
    private String phoneNumber;
    private String verificationCode;
}
