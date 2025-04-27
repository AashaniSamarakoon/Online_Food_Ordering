package com.example.bankingservice.exception;

import java.util.Arrays;

public class BankAccountRepository extends RuntimeException {
    public BankAccountRepository(String message) {
        super(message);
    }

    public Arrays findAll() {
        return BankAccountRepository.this.findAll();
    }
}
