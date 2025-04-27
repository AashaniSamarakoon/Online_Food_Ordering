//package com.example.bankingservice.dto;
//
//import com.example.bankingservice.model.AccountType;
//import lombok.Data;
//
//@Data
//public class BankAccountDTO {
//    private String bankName;
//    private String accountNumber;
//    private String routingNumber;
//    private AccountType accountType;
//}

package com.example.bankingservice.dto;

import com.example.bankingservice.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BankAccountDTO {
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Routing number is required")
    private String routingNumber;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private Long restaurantId; // Add if you need to associate with a restaurant
}