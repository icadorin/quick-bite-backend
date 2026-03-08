package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.ProductResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.mappers.OrderCreateMapper;
import com.quickbite.order_service.mappers.OrderItemCreateMapper;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;
    private final OrderItemCreateMapper itemMapper;
    private final OrderCreateMapper createMapper;
    private final ProductServiceClient productClient;

    @Transactional
    public OrderResponse createOrder(
        OrderRequest request,
        Long userId
    ) {
        productClient.validateRestaurant(request.getRestaurantId());

        Order order = createMapper.toEntity(request);

        order.setUserId(userId);

        List<OrderItem> items = request.getItems().stream()
            .map(itemRequest -> {

                ProductResponse product =
                    productClient.getProduct(itemRequest.getProductId());

                if (product == null) {
                    throw new BusinessRuleViolationException(
                        "Product not found: " + itemRequest.getProductId()
                    );
                }

                if (!Boolean.TRUE.equals(product.getIsAvailable())) {
                    throw new BusinessRuleViolationException(
                        "Product" + itemRequest.getProductId() + "is unavailable"
                    );
                }

                OrderItem item = itemMapper.toEntity(itemRequest);

                item.setOrder(order);
                item.setProductName(product.getName());
                item.setUnitPrice(product.getPrice());

                return item;
            }).toList();

        order.setItems(items);

        order.recalculateTotal();

        Order saved = orderRepository.save(order);

        return responseMapper.toResponse(saved);
    }
}
