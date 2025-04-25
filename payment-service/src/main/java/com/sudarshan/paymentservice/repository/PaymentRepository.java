package com.sudarshan.paymentservice.repository;


import com.sudarshan.paymentservice.entity.Payment;
import com.sudarshan.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // PaymentRepository.java
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    List<Payment> findByRestaurantId(Long restaurantId);
    List<Payment> findByRiderId(Long riderId);
    List<Payment> findByRestaurantIdAndRiderId(Long restaurantId, Long riderId);
}
