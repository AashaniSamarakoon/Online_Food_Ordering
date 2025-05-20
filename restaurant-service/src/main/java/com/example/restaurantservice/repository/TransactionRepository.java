package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByRestaurantId(Long restaurantId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t WHERE t.paymentServiceTransactionId = :paymentServiceId")
    boolean existsByPaymentServiceTransactionId(@Param("paymentServiceId") Long paymentServiceId);
}