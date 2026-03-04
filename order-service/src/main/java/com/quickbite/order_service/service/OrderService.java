package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.domain.OrderCalculator;
import com.quickbite.order_service.dto.*;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.entity.OrderStatusHistory;
import com.quickbite.order_service.mappers.*;
import com.quickbite.order_service.repositories.OrderItemRepository;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.OrderStatusHistoryRepository;
import com.quickbite.order_service.repositories.specifications.OrderSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final OrderAuthorizationService authorizationService;
    private final OrderCalculator orderCalculator;

    public List<OrderResponse> getUserOrders(Long userId) {
        validateId(userId);

        var spec = OrderSpecification.withFilters(
            new OrderFilter(userId, null, null, null, null)
        );

        return responseMapper.toResponseList(
            orderRepository.findAll(spec)
        );
    }

    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        validateId(restaurantId);

        var spec = OrderSpecification.withFilters(
            new OrderFilter(null, restaurantId, null, null, null)
        );

        return responseMapper.toResponseList(
            orderRepository.findAll(spec)
        );
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {

        Order order =
            authorizationService.authorizeUserAccess(orderId, userId);

        return enrichAndMap(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        validateId(userId);

        productClient.validateRestaurant(request.getRestaurantId());

        Map<Long, ProductResponse> products = loadProducts(request.getItems());

        BigDecimal totalAmount = orderCalculator.calculateTotal(request.getItems(), products);

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
        Order order = getCustomerAuthorizedOrder(orderId, userId);

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

        Order order = getCustomerAuthorizedOrder(orderId, userId);

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
            .map(req -> buildOrderItem(order, req, products))
            .toList();

        orderItemRepository.saveAll(items);
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

    private Order getCustomerAuthorizedOrder(Long orderId, Long userId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Order not found with id: " + orderId)
            );

        if (!order.getUserId().equals(userId)) {
            throw new BusinessRuleViolationException(
                "You are not allowed to access this order"
            );
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

    private OrderItem buildOrderItem(
        Order order,
        OrderItemRequest request,
        Map<Long, ProductResponse> products
    ) {
        ProductResponse product =
            getProductOrThrow(request.getProductId(), products);

        OrderItem item = itemCreateMapper.toEntity(request);

        item.setOrder(order);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());

        return item;
    }

    private ProductResponse getProductOrThrow(
        Long productId,
        Map<Long, ProductResponse> products
    ) {
        ProductResponse product = products.get(productId);

        if (product == null) {
            throw new ResourceNotFoundException(
                "Product not found with id: " + productId
            );
        }

        return product;
    }
}
