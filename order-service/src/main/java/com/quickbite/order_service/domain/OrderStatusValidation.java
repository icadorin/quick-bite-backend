package com.quickbite.order_service.domain;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusValidation {

    public void validationTransition(
        Order.OrderStatus current,
        Order.OrderStatus next
    ) {
        boolean valid = switch (current) {
            case PENDING ->
                next == Order.OrderStatus.CONFIRMED ||
                    next == Order.OrderStatus.CANCELLED;

            case CONFIRMED ->
                next == Order.OrderStatus.PREPARING;

            case PREPARING ->
                next == Order.OrderStatus.READY_FOR_PICKUP;

            case READY_FOR_PICKUP ->
                next == Order.OrderStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY ->
                next == Order.OrderStatus.DELIVERED;

            default -> false;
        };

        if (!valid) {
            throw new BusinessRuleViolationException(
                "Invalid Order transition"
            );
        }
    }
}
