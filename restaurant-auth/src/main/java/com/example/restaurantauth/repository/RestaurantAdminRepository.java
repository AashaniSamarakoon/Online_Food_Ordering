//package com.example.restaurantauth.repository;
//
//import com.example.restaurantauth.model.RestaurantAdmin;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface RestaurantAdminRepository extends JpaRepository<RestaurantAdmin, Long> {
//    Optional<RestaurantAdmin> findByEmail(String email);
//
//    Optional<RestaurantAdmin> findByNic(String nic);
//
//    Optional<RestaurantAdmin> findByAccountNumber(String accountNumber);
//
//    Optional<RestaurantAdmin> findByPhone(String phone);
//
//    boolean existsByEmail(String email);
//
//    boolean existsByNic(String nic);
//
//    boolean existsByAccountNumber(String accountNumber);
//
//    boolean existsByPhone(String phone);
//}


package com.example.restaurantauth.repository;

import com.example.restaurantauth.model.RestaurantAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantAdminRepository extends JpaRepository<RestaurantAdmin, Long> {
    Optional<RestaurantAdmin> findByEmail(String email);

    Optional<RestaurantAdmin> findByNic(String nic);

    Optional<RestaurantAdmin> findByAccountNumber(String accountNumber);

    Optional<RestaurantAdmin> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByNic(String nic);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByPhone(String phone);

    List<RestaurantAdmin> findByIsVerifiedTrueAndRoleNot(RestaurantAdmin.Role role);

    List<RestaurantAdmin> findByIsVerifiedFalseAndRoleNot(RestaurantAdmin.Role role);
}