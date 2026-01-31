package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.dtos.*;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.entity.OrderStatusHistory;
import com.quickbite.order_service.mappers.*;
import com.quickbite.order_service.repositories.OrderItemRepository;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.OrderStatusHistoryRepository;
import com.quickbite.order_service.security.AuthContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductServiceClient productClient;

    private final OrderCreateMapper createMapper;
    private final OrderResponseMapper responseMapper;
    private final OrderItemCreateMapper itemCreateMapper;

    public List<OrderResponse> getUserOrders(AuthContext auth) {
        validateId(auth.userId());

        return responseMapper.toResponseList(
            orderRepository.findByUserId(auth.userId())
        );
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        validateId(orderId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Order not found with id: " + orderId)
            );

        return enrichAndMap(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        validateId(userId);

        productClient.validateRestaurant(request.getRestaurantId());

        Map<Long, ProductResponse> products = loadProducts(request.getItems());

        BigDecimal totalAmount = calculateTotal(request.getItems(), products);

        Order order = createMapper.toEntity(request);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));

        Order saveOrder = orderRepository.save(order);

        createItems(saveOrder, request.getItems(), products);
        addHistory(saveOrder, Order.OrderStatus.PENDING, "Order created");

        return enrichAndMap(saveOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
        Long orderId,
        OrderStatusUpdateRequest request,
        Long userId
    ) {
        AuthContext auth = new AuthContext(userId, "CUSTOMER");
        Order order = getAuthorizedOrder(orderId, auth);

        order.setStatus(request.getStatus());

        if (request.getStatus() == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        Order updated = orderRepository.save(order);
        addHistory(updated, request.getStatus(), request.getNotes());

        return enrichAndMap(updated);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Order not found with id: " + orderId)
            );

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessRuleViolationException(
                "Order can only be cancelled while pending"
            );
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        addHistory(updatedOrder, Order.OrderStatus.CANCELLED, "Order cancelled by user");

        return enrichAndMap(updatedOrder);
    }

    private OrderResponse enrichAndMap(Order order) {
        OrderResponse response = responseMapper.toResponse(order);

        RestaurantResponse restaurant =
            productClient.getRestaurant(order.getRestaurantId());

        response.setRestaurantName(restaurant.getName());
        return response;
    }

    private void createItems(
        Order order,
        List<OrderItemRequest> requests,
        Map<Long, ProductResponse> products
    ) {
        List<OrderItem> items = requests.stream()
            .map(req -> {
                ProductResponse product =
                    products.get(req.getProductId());

                if (product == null) {
                    throw new ResourceNotFoundException(
                        "Product not found: " + req.getProductId()
                    );
                }

                OrderItem item = itemCreateMapper.toEntity(req);
                item.setOrder(order);
                item.setProductName(product.getName());
                item.setUnitPrice(product.getPrice());
                return item;
            })
            .toList();

        orderItemRepository.saveAll(items);
    }

    private BigDecimal calculateTotal(
        List<OrderItemRequest> items,
        Map<Long, ProductResponse> products
    ) {
        return items.stream()
            .map(item -> {
                ProductResponse product = products.get(item.getProductId());

                if (product == null || !Boolean.TRUE.equals(product.getIsAvailable())) {
                    throw new BusinessRuleViolationException(
                        "Product not available: " + item.getProductId()
                    );
                }

                return product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void addHistory(Order order, Order.OrderStatus status, String notes) {
        historyRepository.save(
            OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .notes(notes)
                .build()
        );
    }

    private Order getAuthorizedOrder(Long orderId, AuthContext auth) {
        if (auth == null) {
            throw new BusinessRuleViolationException("Authentication context missing");
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Order not found with id: " + orderId)
            );

        switch (auth.role()) {
            case "CUSTOMER" -> {
                if (!order.getUserId().equals(auth.userId())) {
                    throw new BusinessRuleViolationException(
                        "You are not allowed to access this order"
                    );
                }
            }
            case "RESTAURANT" -> {
                //
                // if (!order.getRestaurantId().equals(auth.userId())){}
            }
            case "ADMIN" -> {
                //
            }
            default -> throw new BusinessRuleViolationException("Invalid role");
        }

        return order;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid id");
        }
    }

    private Map<Long, ProductResponse> loadProducts(List<OrderItemRequest> items) {
        return items.stream()
            .map(OrderItemRequest::getProductId)
            .distinct()
            .collect(Collectors.toMap(
                id -> id,
                productClient::getProduct
            ));
    }
}
