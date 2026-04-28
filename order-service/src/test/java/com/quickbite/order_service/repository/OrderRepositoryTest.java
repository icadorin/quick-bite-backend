package com.quickbite.order_service.repository;

import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.model.DeliveryAddress;
import com.quickbite.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    private DeliveryAddress buildAddress() {
        return DeliveryAddress.builder()
            .street(VALID_STREET)
            .number(VALID_NUMBER)
            .city(VALID_CITY)
            .state(VALID_STATE)
            .zipCode(VALID_ZIP_CODE)
            .complement(VALID_COMPLEMENT)
            .build();

    }

    private Order buildOrder(
        Long userId,
        Long restaurantId,
        Order.OrderStatus status
    ) {
        return Order.builder()
            .userId(userId)
            .restaurantId(restaurantId)
            .status(status)
            .deliveryAddress(buildAddress())
            .totalAmount(VALID_TOTAL_AMOUNT)
            .build();
    }

    @Test
    void shouldFindOrderByIdAndUserId() {
        Order saved = repository.save(
            buildOrder(
                VALID_USER_ID,
                OTHER_RESTAURANT_ID,
                Order.OrderStatus.PENDING
            )
        );

        Optional<Order> result =
            repository.findByIdAndUserId(
                saved.getId(),
                VALID_USER_ID
            );

        assertTrue(result.isPresent());
        assertEquals(
            VALID_USER_ID,
            result.get().getUserId()
        );
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotOwnOrder() {
        Order saved = repository.save(
            buildOrder(
                VALID_USER_ID,
                OTHER_RESTAURANT_ID,
                Order.OrderStatus.PENDING
            )
        );

        Optional<Order> result =
            repository.findByIdAndUserId(
                saved.getId(),
                NON_EXISTENT_ID
            );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCountOrderByRestaurantAndStatus() {
        repository.save(
            buildOrder(
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                Order.OrderStatus.PENDING
            )
        );

        repository.save(
            buildOrder(
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                Order.OrderStatus.PENDING
            )
        );

        repository.save(
            buildOrder(
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                Order.OrderStatus.CONFIRMED
            )
        );

        Long count = repository.countByRestaurantAndStatus(
            VALID_RESTAURANT_ID,
            Order.OrderStatus.PENDING
        );

        assertEquals(2L, count);
    }
}
