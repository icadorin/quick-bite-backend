package com.quickbite.order_service.service;

import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.*;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class OrderService {

    private final OrderCreationService creationService;
    private final OrderQueryService queryService;
    private final OrderStatusService statusService;
    private final OrderAuthorizationService authorizationService;

    public List<OrderResponse> getUserOrders(Long userId) {
        return queryService.getUserOrders(userId);
    }

    public OrderResponse getOrderById(
        Long id,
        Long userId,
        UserRole role
    ) {

        Order order =
            authorizationService.authorizeUserAccess(id, userId, role);

        return queryService.getOrderById(order);
    }

    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        return queryService.getRestaurantOrders(restaurantId);
    }

    public OrderResponse createOrder(
        OrderRequest request,
        Long userId
    ) {
        Order order =
            creationService.createOrder(request, userId);

        return queryService.getOrderById(order);
    }

    public OrderResponse updateOrderStatus(
        Long id,
        OrderStatusUpdateRequest request,
        Long userId,
        UserRole role
    ) {
        Order order =
            statusService.updateStatus(
                id,
                userId,
                role,
                request.getStatus(),
                request.getNotes()
            );

        return queryService.getOrderById(order);
    }

    public OrderResponse cancelOrder(
        Long id,
        Long userId,
        UserRole role
    ) {
        Order order =
            statusService.updateStatus(
                id,
                userId,
                role,
                Order.OrderStatus.CANCELLED,
                "Order cancelled by customer"
            );

        return queryService.getOrderById(order);
    }
}
