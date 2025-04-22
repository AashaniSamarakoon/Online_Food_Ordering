package com.restaurant_service.restaurant_service.repository;

import com.restaurant_service.restaurant_service.model.Restaurant;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query(value = """
        SELECT *, (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(r.latitude)) *
                cos(radians(r.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(r.latitude))
            )
        ) AS distance
        FROM restaurant r
        HAVING distance < :radius
        ORDER BY distance
        """, nativeQuery = true)
    List<Restaurant> findNearbyRestaurants(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusInKm
    );
}


