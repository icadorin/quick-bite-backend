package com.quickbite.order_service.repositories;

import com.quickbite.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByRestaurantId(Long restaurantId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);

    List<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByUserIdAndCreateAtBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = :status")
    Long countByRestaurantAndStatus(
        @Param("restaurantId") Long restaurantId,
        @Param("status") Order.OrderStatus status
    );

    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
