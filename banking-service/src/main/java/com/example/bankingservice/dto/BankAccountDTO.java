package com.example.bankingservice.dto;

import com.example.bankingservice.model.AccountType;
import lombok.Data;

@Data
public class BankAccountDTO {
    private String bankName;
    private String accountNumber;
    private String routingNumber;
    private AccountType accountType;
}