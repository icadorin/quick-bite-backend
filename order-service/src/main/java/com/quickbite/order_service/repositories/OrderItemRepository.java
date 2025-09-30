package com.quickbite.order_service.repositories;

import com.quickbite.order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.restaurantId = :restaurantId")
    List<OrderItem> findByRestaurantId(@Param("restaurantId") Long restaurantId);
}
