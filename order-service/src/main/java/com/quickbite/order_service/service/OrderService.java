package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.*;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.*;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.specifications.OrderSpecification;
import com.quickbite.order_service.security.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class OrderService {

    private final OrderCreationService creationService;
    private final OrderQueryService queryService;
    private final OrderStatusService statusService;
    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;

    public List<OrderResponse> getUserOrders(Long userId)  {
        return queryService.getUserOrders(userId);
    }

    public OrderResponse getOrderById(Long id, Long userId, UserRole role) {
        return queryService.getOrderById(id, userId, role);
    }

    public Page<OrderResponse> getRestaurantOrders(Long restaurantId, Pageable pageable) {
        return queryService.getRestaurantOrders(restaurantId, pageable);
    }

    public OrderStatusResponse getRestaurantStats(Long restaurantId) {

        Map<Order.OrderStatus, Long> stats =
            Arrays.stream(Order.OrderStatus.values())
                .collect(Collectors.toMap(
                    status -> status,
                    status -> orderRepository.countByRestaurantAndStatus(restaurantId, status)
                ));

        return new OrderStatusResponse(stats);
    }

    public Page<OrderResponse> searchOrders(
        OrderFilter filter,
        JwtUser user,
        Pageable pageable
    ) {
        OrderFilter securedFilter;

        if (user.role() == UserRole.CUSTOMER) {
            securedFilter = new OrderFilter(
                user.id(),
                null,
                filter.status(),
                filter.startDate(),
                filter.endDate()
            );
        } else if (user.role() == UserRole.RESTAURANT_OWNER) {
            securedFilter = new OrderFilter(
                null,
                user.restaurantId(),
                filter.status(),
                filter.startDate(),
                filter.endDate()
            );
        } else {
            throw new BusinessRuleViolationException("Invalid role for search");
        }

        return orderRepository.findAll(
            OrderSpecification.withFilters(securedFilter),
            pageable
        ).map(responseMapper::toResponse);
    }

    public OrderResponse createOrder(OrderRequest request, Long userId) {
        return creationService.createOrder(request, userId);
    }

    public OrderResponse updateOrderStatus(
        Long id,
        OrderStatusUpdateRequest request,
        Long userId,
        Long restaurantId,
        UserRole role
    ) {
        return statusService.updateStatus(id, request, userId, restaurantId, role);
    }

    public OrderResponse cancelOrder(
        Long id,
        Long userId,
        Long restaurantId,
        UserRole role
    ) {
        return statusService.cancelOrder(id, userId, restaurantId, role);
    }
}
