package com.quickbite.order_service.service;

import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.OrderStatusResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static com.quickbite.order_service.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderResponseMapper responseMapper;

    @InjectMocks
    private OrderQueryService service;

    private Order buildOrder(Long orderId, Long userId) {
        return Order.builder()
            .id(orderId)
            .userId(userId)
            .build();
    }

    private OrderResponse buildOrderResponse() {
        return new OrderResponse();
    }

    @Test
    void shouldReturnUserOrders() {

        List<Order> orders = List.of(
            buildOrder(VALID_ORDER_ID, VALID_USER_ID)
        );

        when(orderRepository.findAll(ArgumentMatchers.<Specification<Order>>any()))
            .thenReturn(orders);
        when(responseMapper.toResponseList(orders))
            .thenReturn(List.of(buildOrderResponse()));

        List<OrderResponse> result = service.getUserOrders(VALID_USER_ID);

        assertEquals(1, result.size());

        verify(orderRepository)
            .findAll(ArgumentMatchers.<Specification<Order>>any());
    }

    @Test
    void shouldReturnOrderByIdForCustomer() {
        Order order = buildOrder(
            VALID_ORDER_ID,
            VALID_USER_ID
        );

        when(orderRepository.findByIdAndUserId(
            VALID_ORDER_ID,
            VALID_USER_ID
        )).thenReturn(Optional.of(order));

        when(responseMapper.toResponse(order))
            .thenReturn(new OrderResponse());

        OrderResponse response = service.getOrderById(
            VALID_ORDER_ID,
            VALID_USER_ID,
            UserRole.CUSTOMER
        );

        assertNotNull(response);

        verify(orderRepository)
            .findByIdAndUserId(
                VALID_ORDER_ID,
                VALID_USER_ID
            );
    }

    @Test
    void shouldThrowWhenCustomerOrderNotFound() {
        when(orderRepository.findByIdAndUserId(
            VALID_ORDER_ID,
            VALID_USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.getOrderById(
                VALID_ORDER_ID,
                VALID_USER_ID,
                UserRole.CUSTOMER
            )
        );
    }

    @Test
    void shouldReturnOrderByIdForAdmin() {
        Order order = buildOrder(
            VALID_ORDER_ID,
            VALID_USER_ID
        );

        when(orderRepository.findById(VALID_ORDER_ID))
            .thenReturn(Optional.of(order));

        when(responseMapper.toResponse(order))
            .thenReturn(new OrderResponse());

        OrderResponse response = service.getOrderById(
            VALID_ORDER_ID,
            null,
            UserRole.ADMIN
        );

        assertNotNull(response);

        verify(orderRepository)
            .findById(VALID_ORDER_ID);
    }

    @Test
    void shouldReturnRestaurantStats() {
        when(orderRepository.countByRestaurantAndStatus(
            eq(VALID_RESTAURANT_ID),
            any()
        )).thenReturn(2L);

        OrderStatusResponse response =
            service.getRestaurantStats(
                VALID_RESTAURANT_ID
            );

        assertNotNull(response);

        verify(
            orderRepository,
            times(Order.OrderStatus.values().length)
        ).countByRestaurantAndStatus(
            eq(VALID_RESTAURANT_ID),
            any()
        );
    }
}
