package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderAuthorizationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderAuthorizationService service;

    @Test
    void shouldAllowAdminAccess() {
        Order order = Order.builder()
            .id(VALID_ORDER_ID)
            .userId(VALID_USER_ID)
            .restaurantId(VALID_RESTAURANT_ID)
            .build();

        when(orderRepository.findById(VALID_ORDER_ID))
            .thenReturn(Optional.of(order));

        Order result = service.authorizeUserAccess(
            VALID_ORDER_ID,
            NON_EXISTENT_ID,
            OTHER_RESTAURANT_ID,
            UserRole.ADMIN
        );

        assertNotNull(result);
    }

    @Test
    void shouldAllowCustomerOwnOrderAccess() {
        Order order = Order.builder()
            .id(VALID_ORDER_ID)
            .userId(VALID_USER_ID)
            .build();

        when(orderRepository.findById(VALID_ORDER_ID))
            .thenReturn(Optional.of(order));

        Order result = service.authorizeUserAccess(
            VALID_ORDER_ID,
            VALID_USER_ID,
            null,
            UserRole.CUSTOMER
        );

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.authorizeUserAccess(
                VALID_ORDER_ID,
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                UserRole.ADMIN
            )
        );
    }

    @Test
    void shouldThrowWhenAccessDenied() {
        Order order = Order.builder()
            .userId(NON_EXISTENT_ID)
            .restaurantId(OTHER_RESTAURANT_ID)
            .build();

        when(orderRepository.findById(VALID_ORDER_ID))
            .thenReturn(Optional.of(order));

        assertThrows(
            BusinessRuleViolationException.class,
            () -> service.authorizeUserAccess(
                VALID_ORDER_ID,
                VALID_USER_ID,
                VALID_RESTAURANT_ID,
                UserRole.CUSTOMER
            )
        );
    }
}
