package com.quickbite.order_service.repositories;

import com.quickbite.order_service.entity.Order.OrderStatus;
import com.quickbite.order_service.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    @Query("""
        SELECT osh
        FROM OrderStatusHistory osh
        WHERE osh.order.id = :orderId
            AND osh.status = :status
    """)
    List<OrderStatusHistory> findByOrderIdAndStatus(
            @Param("orderId") Long orderId,
            @Param("status") OrderStatus status
    );

    @Query("""
        SELECT osh
        FROM OrderStatusHistory osh
        WHERE osh.order.restaurantId = :restaurantId
    """)
    List<OrderStatusHistory> findByRestaurantId(@Param("restaurantId") Long restaurantId);

    Optional<OrderStatusHistory> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}
