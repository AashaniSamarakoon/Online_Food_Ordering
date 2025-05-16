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

    // Basic account operations
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByRoutingNumber(String routingNumber);

    // Search operations
    @Query("SELECT ba FROM BankAccount ba WHERE " +
            "ba.accountNumber LIKE %:searchTerm% OR " +
            "ba.bankName LIKE %:searchTerm% OR " +
            "ba.routingNumber LIKE %:searchTerm%")
    List<BankAccount> searchAccounts(@Param("searchTerm") String searchTerm);

    // Filter by account type
    List<BankAccount> findByAccountType(AccountType accountType);

    // Filter by bank name
    List<BankAccount> findByBankNameContainingIgnoreCase(String bankName);
    List<BankAccount> findByBankNameContainingIgnoreCaseAndAccountType(
            String bankName, AccountType accountType);

    // Balance queries
    List<BankAccount> findByBalanceGreaterThan(double amount);
    List<BankAccount> findByBalanceLessThan(double amount);
    List<BankAccount> findByBalanceBetween(double minAmount, double maxAmount);
    List<BankAccount> findByBalanceGreaterThanOrderByBalanceDesc(double amount);

    // Balance operations
    @Modifying
    @Query("UPDATE BankAccount ba SET ba.balance = ba.balance + :amount WHERE ba.id = :id")
    void deposit(@Param("id") Long id, @Param("amount") double amount);

    @Modifying
    @Query("UPDATE BankAccount ba SET ba.balance = ba.balance - :amount WHERE ba.id = :id AND ba.balance >= :amount")
    int withdraw(@Param("id") Long id, @Param("amount") double amount);

    // Transfer operation
    @Modifying
    @Query("UPDATE BankAccount ba SET " +
            "ba.balance = CASE " +
            "WHEN ba.id = :fromId THEN ba.balance - :amount " +
            "WHEN ba.id = :toId THEN ba.balance + :amount " +
            "ELSE ba.balance END " +
            "WHERE ba.id IN (:fromId, :toId) " +
            "AND (SELECT b.balance FROM BankAccount b WHERE b.id = :fromId) >= :amount")
    int transfer(@Param("fromId") Long fromId,
                 @Param("toId") Long toId,
                 @Param("amount") double amount);

    // Account verification
    @Query("SELECT CASE WHEN COUNT(ba) > 0 THEN true ELSE false END " +
            "FROM BankAccount ba WHERE " +
            "ba.accountNumber = :accountNumber AND " +
            "ba.routingNumber = :routingNumber AND " +
            "ba.bankName = :bankName")
    boolean verifyAccountDetails(
            @Param("bankName") String bankName,
            @Param("accountNumber") String accountNumber,
            @Param("routingNumber") String routingNumber);
}