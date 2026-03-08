package com.quickbite.order_service.service;

import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.dto.*;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.mappers.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return queryService.getRestaurantStats(restaurantId);
    }

    public Page<OrderResponse> searchOrders(OrderFilter filter, Pageable pageable) {
        return queryService.searchOrders(filter, pageable);
    }

    public OrderResponse createOrder(OrderRequest request, Long userId) {
        return creationService.createOrder(request, userId);
    }

    public OrderResponse updateOrderStatus(
        Long id,
        OrderStatusUpdateRequest request,
        Long userId,
        UserRole role
    ) {
        return statusService.updateStatus(id, request, userId, role);
    }

    public OrderResponse cancelOrder(
        Long id,
        Long userId,
        UserRole role
    ) {
        return statusService.cancelOrder(id, userId, role);
    }
}
