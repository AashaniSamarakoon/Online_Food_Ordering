package com.example.bankingservice.repository;

import com.example.bankingservice.model.AccountType;
import com.example.bankingservice.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByBankNameContainingIgnoreCaseAndAccountType(
            String bankName, AccountType accountType);

    List<BankAccount> findByAccountType(AccountType accountType);

    List<BankAccount> findByBalanceGreaterThan(double amount);

    List<BankAccount> findByBalanceLessThan(double amount);

    List<BankAccount> findByBalanceBetween(double minAmount, double maxAmount);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT ba FROM BankAccount ba WHERE ba.accountNumber LIKE %:searchTerm% OR ba.bankName LIKE %:searchTerm%")
    List<BankAccount> searchAccounts(@Param("searchTerm") String searchTerm);

    @Modifying
    @Query("UPDATE BankAccount ba SET ba.balance = ba.balance + :amount WHERE ba.id = :id")
    void deposit(@Param("id") Long id, @Param("amount") double amount);

    @Modifying
    @Query("UPDATE BankAccount ba SET ba.balance = ba.balance - :amount WHERE ba.id = :id AND ba.balance >= :amount")
    int withdraw(@Param("id") Long id, @Param("amount") double amount);

    List<BankAccount> findByBalanceGreaterThanOrderByBalanceDesc(double amount);
}