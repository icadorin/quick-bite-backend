package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByIsActiveTrue();

    List<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByCuisineTypeAndIsActiveTrue(String cuisineType);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Restaurant> searchActiveRestaurantsByName(@Param("name") String name);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.rating >= :minRating")
    List<Restaurant> findActiveRestaurantsWithMinRating(@Param("minRating") Double minRating);

    Optional<Restaurant> findByIdAndIsActiveTrue(Long id);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}