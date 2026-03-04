package com.quickbite.order_service.repositories;

import com.quickbite.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends
    JpaRepository<Order, Long>,
    JpaSpecificationExecutor<Order> {

    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.restaurantId = :restaurantId
            AND o.status = :status
    """)
    Long countByRestaurantAndStatus(
        @Param("restaurantId") Long restaurantId,
        @Param("status") Order.OrderStatus status
    );

    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
