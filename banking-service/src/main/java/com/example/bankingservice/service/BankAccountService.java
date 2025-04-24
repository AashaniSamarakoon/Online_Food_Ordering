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
    private final BankAccountRepository bankAccountRepository; // Fixed variable name

    @Transactional
    public BankAccountResponse createAccount(BankAccountDTO accountDTO) {
        if (bankAccountRepository.existsByAccountNumber(accountDTO.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists");
        }

        BankAccount account = new BankAccount();
        account.setBankName(accountDTO.getBankName());
        account.setAccountNumber(accountDTO.getAccountNumber());
        account.setRoutingNumber(accountDTO.getRoutingNumber());
        account.setAccountType(accountDTO.getAccountType());
//        account.setBalance(accountDTO.getBalance() != null ? accountDTO.getBalance() : 0.0); // Handle null balance

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

        if (accountDTO.getBankName() != null) {
            account.setBankName(accountDTO.getBankName());
        }
        if (accountDTO.getAccountType() != null) {
            account.setAccountType(accountDTO.getAccountType());
        }
//        if (accountDTO.getBalance() != null) {
//            account.setBalance(accountDTO.getBalance());
//        }

        BankAccount updatedAccount = bankAccountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    private BankAccountResponse mapToResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .maskedAccountNumber(maskAccountNumber(account.getAccountNumber()))
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