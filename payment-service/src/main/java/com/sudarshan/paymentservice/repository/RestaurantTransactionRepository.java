package com.sudarshan.paymentservice.repository;

import com.sudarshan.paymentservice.entity.RestaurantTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTransactionRepository extends JpaRepository<RestaurantTransaction, Long> {

    List<RestaurantTransaction> findByRestaurantId(Long restaurantId);

    List<RestaurantTransaction> findByRestaurantIdAndDate(Long restaurantId, java.time.LocalDate date);
}
