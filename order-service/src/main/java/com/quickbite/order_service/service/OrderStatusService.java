package com.quickbite.order_service.service;

import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.service.validation.OrderStatusValidation;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.OrderStatusUpdateRequest;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderAuthorizationService authorizationService;
    private final OrderStatusValidation statusValidation;
    private final OrderResponseMapper responseMapper;

    @Transactional
    public OrderResponse updateStatus(
        Long id,
        OrderStatusUpdateRequest request,
        Long userId,
        UserRole role
    ) {
        Order order =
            authorizationService.authorizeUserAccess(
                id,
                userId,
                role
            );

        statusValidation.validateTransition(
            order.getStatus(),
            request.getStatus()
        );

        order.changeStatus(
            request.getStatus(),
            request.getNotes()
        );

        return responseMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(
        Long id,
        Long userId,
        UserRole role
    ) {
        Order order =
            authorizationService.authorizeUserAccess(
                id,
                userId,
                role
            );

        statusValidation.validateTransition(
            order.getStatus(),
            Order.OrderStatus.CANCELLED
        );

        order.changeStatus(
            Order.OrderStatus.CANCELLED,
            "Order cancelled"
        );

        return responseMapper.toResponse(order);
    }
}
