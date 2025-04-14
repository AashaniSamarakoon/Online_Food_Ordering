package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.dto.*;

public interface AdminService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse registerAdmin(AdminRegistrationRequest registrationRequest); // New method

}