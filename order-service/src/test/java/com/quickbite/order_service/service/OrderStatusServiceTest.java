package com.quickbite.order_service.service;

import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.OrderStatusUpdateRequest;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderStatusServiceTest {

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseMapper responseMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderStatusService service;

    private Order buildOrder(
        Long id,
        Order.OrderStatus status
    ) {
        return Order.builder()
            .id(id)
            .status(status)
            .build();
    }

    private OrderResponse buildOrderResponse() {
        return new OrderResponse();
    }

    @Test
    void shouldUpdateStatus() {

        Order order = buildOrder(
            VALID_ORDER_ID,
            Order.OrderStatus.PENDING
        );

        OrderStatusUpdateRequest request =
            new OrderStatusUpdateRequest(
                Order.OrderStatus.CONFIRMED,
                VALID_REASON
            );

        when(
            authorizationService.authorizeUserAccess(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(order);

        when(orderRepository.save(order))
            .thenReturn(order);
        when(responseMapper.toResponse(order))
            .thenReturn(buildOrderResponse());

        OrderResponse response = service.updateStatus(
            VALID_ORDER_ID,
            request,
            VALID_USER_ID,
            VALID_RESTAURANT_ID,
            UserRole.RESTAURANT_OWNER
        );

        assertNotNull(response);

        verify(orderRepository).save(order);
    }

    @Test
    void shouldCancelOrder() {

        Order order = buildOrder(
            VALID_ORDER_ID,
            Order.OrderStatus.PENDING
        );

        when(
            authorizationService.authorizeUserAccess(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(order);

        when(orderRepository.save(order))
            .thenReturn(order);

        when(responseMapper.toResponse(order))
            .thenReturn(buildOrderResponse());

        OrderResponse response = service.cancelOrder(
            VALID_ORDER_ID,
            VALID_USER_ID,
            VALID_RESTAURANT_ID,
            UserRole.CUSTOMER
        );

        assertNotNull(response);

        assertEquals(
            Order.OrderStatus.CANCELLED,
            order.getStatus()
        );

        verify(orderRepository).save(order);
    }
}
