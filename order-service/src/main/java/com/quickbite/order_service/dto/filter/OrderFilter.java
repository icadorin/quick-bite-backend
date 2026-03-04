package com.quickbite.order_service.dto.filter;

import com.quickbite.order_service.entity.Order;

import java.time.LocalDateTime;

public record OrderFilter(
    Long userId,
    Long restaurantId,
    Order.OrderStatus status,
    LocalDateTime startDate,
    LocalDateTime endDate
) {}
