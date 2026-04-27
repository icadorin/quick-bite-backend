package com.quickbite.order_service.entity;

import org.junit.jupiter.api.Test;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderItemTest {

    @Test
    void shouldCalculateTotalPrice() {
        OrderItem item = OrderItem.builder()
            .quantity(DOUBLE_QUANTITY)
            .unitPrice((ITEM_PRICE))
            .build();

        item.calculateTotalPrice();

        assertEquals(
            0,
            item.getTotalPrice().compareTo(EXPECTED_TOTAL_PRICE)
        );
    }

    @Test
    void shouldThrowWhenMissingPrice() {
        OrderItem item = OrderItem.builder()
            .quantity(DOUBLE_QUANTITY)
            .build();

        assertThrows(
            IllegalStateException.class,
            item::calculateTotalPrice
        );
    }
}
