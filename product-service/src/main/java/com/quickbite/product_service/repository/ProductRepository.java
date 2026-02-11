package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends
    JpaRepository<Product, Long>,
    JpaSpecificationExecutor<Product> {

    List<Product> findByIsFeaturedTrueAndIsAvailableTrue();

    Long countByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Product p
        WHERE p.id = :productId
            AND p.restaurant.owner.email = :ownerEmail
            AND p.isAvailable = true
    """)
    boolean existsByAndRestaurantOwnerEmail(
        @Param("productId") Long productId,
        @Param("ownerEmail") String ownerEmail
    );

    Long countByCategoryIdAndIsAvailableTrue(Long categoryId);
}