package com.quickbite.order_service.service;

import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.domain.OrderCalculator;
import com.quickbite.order_service.dto.OrderItemRequest;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.ProductResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.entity.OrderStatusHistory;
import com.quickbite.order_service.mappers.OrderCreateMapper;
import com.quickbite.order_service.mappers.OrderItemCreateMapper;
import com.quickbite.order_service.repositories.OrderItemRepository;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.OrderStatusHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductServiceClient productClient;
    private final OrderCalculator calculator;
    private final OrderCreateMapper createMapper;
    private final OrderItemCreateMapper itemMapper;

    @Transactional
    public Order createOrder(
        OrderRequest request,
        Long userId
    ) {
        productClient.validateRestaurant(request.getRestaurantId());

        Map<Long, ProductResponse> products =
            loadProduct(request.getItems());

        BigDecimal total =
            calculator.calculateTotal(request.getItems(), products);

        Order order = createMapper.toEntity(request);

        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setEstimatedDeliveryTime(
            LocalDateTime.now().plusMinutes(45)
        );

        order.initializeHistory("Order created");

        Order saved = orderRepository.save(order);

        createItems(saved, request.getItems(), products);

        return saved;
    }

    private void createItems(
        Order order,
        List<OrderItemRequest> requests,
        Map<Long, ProductResponse> products
    ) {
        List<OrderItem> items = requests.stream()
            .map(req -> buildItem(order, req, products))
            .toList();

        orderItemRepository.saveAll(items);
    }

    private OrderItem buildItem(
        Order order,
        OrderItemRequest request,
        Map<Long, ProductResponse> products
    ) {
        ProductResponse product = products.get(request.getProductId());

        if (product == null) {
            throw new ResourceNotFoundException(
                "Product not found"
            );
        }

        OrderItem item = itemMapper.toEntity(request);

        item.setOrder(order);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());

        return item;
    }

    private Map<Long, ProductResponse> loadProduct(
        List<OrderItemRequest> items
    ) {
        return items.stream()
            .map(OrderItemRequest::getProductId)
            .distinct()
            .collect(Collectors.toMap(
                id -> id,
                productClient::getProduct
            ));
    }
}
