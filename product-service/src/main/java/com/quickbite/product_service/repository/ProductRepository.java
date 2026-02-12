package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends
    JpaRepository<Product, Long>,
    JpaSpecificationExecutor<Product> {

    List<Product> findByIsFeaturedTrueAndIsAvailableTrue();

    Long countByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    boolean existsByIdAndRestaurantOwnerIdAndIsAvailableTrue(Long productId, Long restaurantId);

    Long countByCategoryIdAndIsAvailableTrue(Long categoryId);
}