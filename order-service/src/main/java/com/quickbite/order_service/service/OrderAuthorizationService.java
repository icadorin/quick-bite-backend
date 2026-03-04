package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAuthorizationService {

    private final OrderRepository orderRepository;

    public Order authorizeUserAccess(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Order not found")
            );

        if (!order.getUserId().equals(userId)) {
            throw new BusinessRuleViolationException(
                "Access denied"
            );
        }

        return order;
    }
}
