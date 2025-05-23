//package com.example.bankingservice.dto;
//
//import com.example.bankingservice.model.AccountType;
//import jdk.jshell.Snippet;
//import lombok.Builder;
//import lombok.Data;
//import java.util.List;
//
//@Data
//@Builder
//public class BankAccountResponse {
//    private Long id;
//    private String bankName;
//    private String maskedAccountNumber;
//    private AccountType accountType;
//    private double balance;
//
//
//}

package com.example.bankingservice.dto;

import com.example.bankingservice.model.AccountType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountResponse {
    private Long id;
    private String bankName;
    private String maskedAccountNumber;
    private String routingNumber; // Added
    private AccountType accountType;
    private String accountTypeDisplay; // Added for frontend display
    private double balance;
}