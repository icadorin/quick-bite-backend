package com.quickbite.order_service.entity;

import com.quickbite.core.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderTest {

    @Test
    void shouldChangeStatusSuccessfully() {
        Order order = Order.builder()
            .status(Order.OrderStatus.PENDING)
            .build();

        order.changeStatus(
            Order.OrderStatus.CONFIRMED,
            CONFIRMED_REASON
        );

        assertEquals(
            Order.OrderStatus.CONFIRMED,
            order.getStatus()
        );

        assertEquals(1, order.getStatusHistory().size());
    }

    @Test
    void shouldThrowWhenTransitionIsInvalid() {
        Order order = Order.builder()
            .status(Order.OrderStatus.PENDING)
            .build();

        assertThrows(
            BusinessRuleViolationException.class,
            () -> order.changeStatus(
                Order.OrderStatus.DELIVERED,
                INVALID_REASON
            )
        );
    }

    @Test
    void shouldRecalculateTotal() {
        Order order = new Order();

        OrderItem item1 = OrderItem.builder()
            .totalPrice(FIRST_ITEM_TOTAL)
            .build();

        OrderItem item2 = OrderItem.builder()
            .totalPrice(SECOND_ITEM_TOTAL)
            .build();

        order.addItem(item1);
        order.addItem(item2);

        order.recalculateTotal();

        assertEquals(
            0,
            order.getTotalAmount().compareTo(RECALCULATED_TOTAL)
        );
    }
}
