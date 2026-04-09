package com.quickbite.order_service.service;

import com.quickbite.order_service.dto.OrderResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderResponseMapper responseMapper;

    @InjectMocks
    private OrderQueryService service;

    @Test
    void shouldReturnUserOrders() {

        List<Order> orders = List.of(new Order());

        when(orderRepository.findAll(
            ArgumentMatchers.<Specification<Order>>any()
        )).thenReturn(orders);
        when(responseMapper.toResponseList(orders))
            .thenReturn(List.of(new OrderResponse()));

        List<OrderResponse> result = service.getUserOrders(1L);

        assertEquals(1, result.size());
        verify(orderRepository).findAll(
            ArgumentMatchers.<Specification<Order>>any()
        );
    }
}
