package com.example.bankingservice.controller;


import com.example.bankingservice.dto.*;
import com.example.bankingservice.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping("/accounts")
    public ResponseEntity<BankAccountResponse> createAccount(@RequestBody BankAccountDTO accountDTO) {
        BankAccountResponse response = bankAccountService.createAccount(accountDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

//    @PostMapping("/transactions")
//    public ResponseEntity<TransactionDTO> addTransaction(@RequestBody TransactionDTO transactionDTO) {
//        TransactionDTO response = bankAccountService.addTransaction(transactionDTO);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }
}