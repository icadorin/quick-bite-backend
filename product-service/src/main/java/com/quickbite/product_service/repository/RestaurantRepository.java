package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RestaurantRepository extends
    JpaRepository<Restaurant, Long>,
    JpaSpecificationExecutor<Restaurant> {

    Optional<Restaurant> findByIdAndIsActiveTrue(Long id);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByNameAndOwnerId(String name, Long ownerId);

    boolean existsByNameAndOwnerIdAndIdNot(
        String name,
        Long ownerId,
        Long id
    );
}