package com.example.bankingservice.controller;

import com.example.bankingservice.dto.BankAccountDTO;
import com.example.bankingservice.dto.BankAccountResponse;
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

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccountResponse> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(bankAccountService.getAccountById(id));
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<BankAccountResponse> updateAccount(
            @PathVariable Long id,
            @RequestBody BankAccountDTO accountDTO) {
        return ResponseEntity.ok(bankAccountService.updateAccount(id, accountDTO));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        bankAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}