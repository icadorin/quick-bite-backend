package com.quickbite.order_service.service;

import com.quickbite.core.security.UserRole;
import com.quickbite.order_service.domain.OrderStatusValidation;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderStatusHistory;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.OrderStatusHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderAuthorizationService authorizationService;
    private final OrderStatusValidation statusValidation;
    private final OrderStatusHistoryRepository historyRepository;

    @Transactional
    public Order updateStatus(
        Long orderId,
        Long userId,
        UserRole role,
        Order.OrderStatus newStatus,
        String notes
    ) {
        Order order =
            authorizationService.authorizeUserAccess(orderId, userId, role);

        statusValidation.validationTransition(
            order.getStatus(),
            newStatus
        );

        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);

        historyRepository.save(
            OrderStatusHistory.builder()
                .order(saved)
                .status(newStatus)
                .notes(notes)
                .build()
        );

        return saved;
    }
}
