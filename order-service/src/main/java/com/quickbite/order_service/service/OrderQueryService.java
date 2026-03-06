package com.quickbite.order_service.service;

import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.specifications.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;

    public List<OrderResponse> getUserOrders(Long userId) {

        List<Order> orders = orderRepository.findAll(
            OrderSpecification.withFilters(
                new OrderFilter(userId, null, null, null, null)
            )
        );

        return responseMapper.toResponseList(orders);
    }

    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {

        List<Order> orders = orderRepository.findAll(
            OrderSpecification.withFilters(
                new OrderFilter(null, restaurantId, null, null, null)
            )
        );

        return responseMapper.toResponseList(orders);
    }

    public OrderResponse getOrderById(
        Long id,
        Long userId,
        UserRole role
    ) {

        Order order;

        if (role == UserRole.CUSTOMER) {
            order = orderRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else {
            order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        }

        return responseMapper.toResponse(order);
    }
}
