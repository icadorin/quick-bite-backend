package com.quickbite.order_service.service;

import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.OrderStatusResponse;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.specifications.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<OrderResponse> getRestaurantOrders(Long restaurantId, Pageable pageable) {

        Page<Order> orders = orderRepository.findAll(
            OrderSpecification.withFilters(
                new OrderFilter(null, restaurantId, null, null, null)
            ),
            pageable
        );

        return orders.map(responseMapper::toResponse);
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

    public Page<OrderResponse> searchOrders(OrderFilter filter, Pageable pageable) {

        Page<Order> orders = orderRepository.findAll(
            OrderSpecification.withFilters(filter),
            pageable
        );
            return orders.map(responseMapper::toResponse);
    }

    public OrderStatusResponse getRestaurantStats(Long restaurantId) {

        long pending = orderRepository.countByRestaurantAndStatus(
            restaurantId,
            Order.OrderStatus.PENDING
        );

        long preparing =  orderRepository.countByRestaurantAndStatus(
            restaurantId,
            Order.OrderStatus.PREPARING
        );

        long delivered = orderRepository.countByRestaurantAndStatus(
            restaurantId,
            Order.OrderStatus.DELIVERED
        );

        long cancelled = orderRepository.countByRestaurantAndStatus(
            restaurantId,
            Order.OrderStatus.CANCELLED
        );

        return new OrderStatusResponse(
            pending,
            preparing,
            delivered,
            cancelled
        );
    }
}
