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

    @Test
    void shouldUpdateStatus() {

        Order order = Order.builder().build();

        OrderStatusUpdateRequest request =
            new OrderStatusUpdateRequest(Order.OrderStatus.CONFIRMED, "ok");

        when(authorizationService.authorizeUserAccess(any(), any(), any(), any()))
            .thenReturn(order);

        when(orderRepository.save(order)).thenReturn(order);
        when(responseMapper.toResponse(order)).thenReturn(new OrderResponse());

        OrderResponse response = service.updateStatus(
            1L, request, 1L, 1L, UserRole.RESTAURANT_OWNER
        );

        assertNotNull(response);
        verify(orderRepository).save(order);
    }
}
