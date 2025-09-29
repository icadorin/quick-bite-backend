package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    List<Product> findByCategoryIdAndIsAvailableTrue(Long categoryId);

    List<Product> findByRestaurantIdAndCategoryIdAndIsAvailableTrue(Long restaurantId, Long categoryId);

    @Query("""
    SELECT p
    FROM Product p
    WHERE p.isAvailable = true
      AND p.restaurant.isActive = true
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Product> searchAvailableProductsByName(@Param("name") String name);

    @Query("""
    SELECT p
    FROM Product p
    WHERE p.isAvailable = true
      AND p.restaurant.id = :restaurantId
      AND p.price BETWEEN :minPrice AND :maxPrice
    """)
    List<Product> findAvailableProductsByRestaurantAndPriceRange(
            @Param("restaurantId") Long restaurantId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    List<Product> findByIsFeaturedTrueAndIsAvailableTrue();

    Long countByRestaurantIdAndIsAvailableTrue(Long restaurantId);
}