package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAuthorizationService {

    private final OrderRepository orderRepository;

    public Order authorizeUserAccess(
        Long orderId,
        Long userId,
        Long restaurantId,
        UserRole role
    ) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Order not found")
            );

        if (role == UserRole.ADMIN) {
            return order;
        }

        if (role == UserRole.CUSTOMER
            && order.getUserId().equals(userId)) {
            return order;
        }

        if (role == UserRole.RESTAURANT_OWNER
            && order.getRestaurantId().equals(restaurantId)) {
            return order;
        }

        throw new BusinessRuleViolationException("Access denied");
    }
}
