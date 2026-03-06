package com.quickbite.order_service.domain;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.entity.Order;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusValidation {

    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> TRANSITIONS =
        new EnumMap<>(Order.OrderStatus.class);

    static {

        TRANSITIONS.put(
            Order.OrderStatus.PENDING,
            EnumSet.of(
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.CANCELLED
            )
        );

        TRANSITIONS.put(
            Order.OrderStatus.CONFIRMED,
            EnumSet.of(Order.OrderStatus.PREPARING)
        );

        TRANSITIONS.put(
            Order.OrderStatus.PREPARING,
            EnumSet.of(Order.OrderStatus.READY_FOR_PICKUP)
        );

        TRANSITIONS.put(
            Order.OrderStatus.READY_FOR_PICKUP,
            EnumSet.of(Order.OrderStatus.OUT_FOR_DELIVERY)
        );

        TRANSITIONS.put(
            Order.OrderStatus.OUT_FOR_DELIVERY,
            EnumSet.of(Order.OrderStatus.DELIVERED)
        );
    }

    public void validateTransition(
        Order.OrderStatus current,
        Order.OrderStatus next
    ) {
        Set<Order.OrderStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(next)) {
            throw new BusinessRuleViolationException(
                "Invalid order status transition: " + current + " -> " + next
            );
        }
    }
}
