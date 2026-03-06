package com.quickbite.order_service.service;

import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderCreateMapper;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;

    private final OrderCreateMapper createMapper;

    @Transactional
    public OrderResponse createOrder(
        OrderRequest request,
        Long userId
    ) {
        Order order = createMapper.toEntity(request);

        order.setUserId(userId);

        Order saved = orderRepository.save(order);

        return responseMapper.toResponse(saved);
    }
}
