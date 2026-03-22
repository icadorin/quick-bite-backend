package com.quickbite.order_service.dto;

import com.quickbite.order_service.entity.Order;

import java.util.Map;

public record OrderStatusResponse(
    Map<Order.OrderStatus, Long> status
) {}
