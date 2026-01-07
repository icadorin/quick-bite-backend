package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    List<Product> findByCategoryIdAndIsAvailableTrue(Long categoryId);

    List<Product> findByIsAvailableTrue();

    List<Product> findByRestaurantIdAndCategoryIdAndIsAvailableTrue(Long restaurantId, Long categoryId);

    List<Product> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name);

    List<Product> findByIsFeaturedTrueAndIsAvailableTrue();

    Long countByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    Long countByCategoryIdAndIsAvailableTrue(Long categoryId);

    List<Product> findByRestaurantIdAndPriceBetweenAndIsAvailableTrue(
        Long restaurantId,
        BigDecimal minPrice,
        BigDecimal maxPrice
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.isAvailable = true
          AND p.restaurant.isActive = true
          AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Product> searchAvailableProductsByName(@Param("name") String name);
}