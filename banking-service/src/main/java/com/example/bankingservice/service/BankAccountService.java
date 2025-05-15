package com.example.bankingservice.service;

import com.example.bankingservice.dto.BankAccountDTO;
import com.example.bankingservice.dto.BankAccountResponse;
import com.example.bankingservice.exception.ResourceNotFoundException;
import com.example.bankingservice.model.BankAccount;
import com.example.bankingservice.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    @Transactional
    public BankAccountResponse createAccount(BankAccountDTO accountDTO) {
        // Validate account number uniqueness
        if (bankAccountRepository.existsByAccountNumber(accountDTO.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists");
        }

        // Validate routing number uniqueness if needed
        if (bankAccountRepository.existsByRoutingNumber(accountDTO.getRoutingNumber())) {
            throw new IllegalArgumentException("Routing number already exists");
        }

        BankAccount account = new BankAccount();
        account.setBankName(accountDTO.getBankName());
        account.setAccountNumber(accountDTO.getAccountNumber());
        account.setRoutingNumber(accountDTO.getRoutingNumber());
        account.setAccountType(accountDTO.getAccountType());
        account.setBalance(0.0); // Initialize balance to 0

        BankAccount savedAccount = bankAccountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    public BankAccountResponse getAccountById(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return mapToResponse(account);
    }

    public List<BankAccountResponse> getAllAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!bankAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account not found with id: " + id);
        }
        bankAccountRepository.deleteById(id);
    }

    @Transactional
    public BankAccountResponse updateAccount(Long id, BankAccountDTO accountDTO) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        // Update fields if they are provided in the DTO
        if (accountDTO.getBankName() != null) {
            account.setBankName(accountDTO.getBankName());
        }
        if (accountDTO.getAccountNumber() != null &&
                !accountDTO.getAccountNumber().equals(account.getAccountNumber())) {
            if (bankAccountRepository.existsByAccountNumber(accountDTO.getAccountNumber())) {
                throw new IllegalArgumentException("New account number already exists");
            }
            account.setAccountNumber(accountDTO.getAccountNumber());
        }
        if (accountDTO.getRoutingNumber() != null &&
                !accountDTO.getRoutingNumber().equals(account.getRoutingNumber())) {
            if (bankAccountRepository.existsByRoutingNumber(accountDTO.getRoutingNumber())) {
                throw new IllegalArgumentException("New routing number already exists");
            }
            account.setRoutingNumber(accountDTO.getRoutingNumber());
        }
        if (accountDTO.getAccountType() != null) {
            account.setAccountType(accountDTO.getAccountType());
        }

        BankAccount updatedAccount = bankAccountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    // Additional business methods
    @Transactional
    public BankAccountResponse deposit(Long accountId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(account.getBalance() + amount);
        BankAccount updatedAccount = bankAccountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    @Transactional
    public BankAccountResponse withdraw(Long accountId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance() - amount);
        BankAccount updatedAccount = bankAccountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    private BankAccountResponse mapToResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .maskedAccountNumber(maskAccountNumber(account.getAccountNumber()))
                .routingNumber(account.getRoutingNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .build();
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}