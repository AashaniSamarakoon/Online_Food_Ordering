//package com.example.restaurantauth.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class RegisterRequest {
//    private String email;
//    private String password;
//    private String restaurantName;
//    private String phone;
//}

package com.example.restaurantauth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    @NotBlank
    private String restaurantName;

    @NotBlank
    private String ownerName;

    @NotBlank @Pattern(regexp = "^[0-9]{9}[vVxX]$")
    private String nic;

    @NotBlank @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @NotBlank
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String bankAccountOwner;

    @NotBlank
    private String bankName;

    @NotBlank
    private String branchName;

    @NotBlank
    private String accountNumber;
}