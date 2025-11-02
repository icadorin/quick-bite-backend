package com.quickbite.order_service.service;

import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.client.RestaurantServiceClient;
import com.quickbite.order_service.dtos.*;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.entity.OrderStatusHistory;
import com.quickbite.order_service.exception.ResourceNotFoundException;
import com.quickbite.order_service.repositories.OrderItemRepository;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.OrderStatusHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductServiceClient productServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;

    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id" + orderId));

        return mapToResponse(order);
    }

    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        log.info("Create order for user: {}", userId);

        restaurantServiceClient.validateRestaurant(request.getRestaurantId());

        BigDecimal totalAmount = calculateOrderTotal(request.getItems());

        Order order = Order.builder()
            .userId(userId)
            .restaurantId(request.getRestaurantId())
            .deliveryAddress(request.getDeliveryAddress())
            .customerNotes(request.getCustomerNotes())
            .paymentMethod(request.getPaymentMethod())
            .totalAmount(totalAmount)
            .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(45))
            .build();

        Order saveOrder = orderRepository.save(order);

        createOrderItems(saveOrder, request.getItems());

        addStatusHistory(saveOrder, Order.OrderStatus.PENDING, "Order created");

        log.info("Order created sucessfully with id: {}", saveOrder.getId());
        return mapToResponse(saveOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not find with id: " + orderId));

        if (!order.getUserId().equals(userId) && !order.getRestaurantId().equals(userId)) {
            throw new SecurityException("Not authorized to update this order");
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        if (request.getStatus() == Order.OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        Order updateOrder = orderRepository.save(order);

        addStatusHistory(updateOrder, request.getStatus(), request.getNotes());

        log.info("Order {} status updated from {} to {}", orderId, oldStatus, request.getStatus());
        return mapToResponse(updateOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) throws IllegalAccessException {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalAccessException("Order can only be cancelled while pending");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        addStatusHistory(updatedOrder, Order.OrderStatus.CANCELLED, "Order cancelled by user");

        log.info("Order {} cancelled by user {}", orderId, userId);
        return mapToResponse(updatedOrder);
    }

    private BigDecimal calculateOrderTotal(List<OrderItemRequest> items) {
        return items.stream()
            .map(item -> {
                ProductResponse product = productServiceClient.getProduct(item.getProductId());
                return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void createOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = itemRequests.stream()
            .map(itemRequest -> {
                ProductResponse product = productServiceClient.getProduct(itemRequest.getProductId());

                return OrderItem.builder()
                    .order(order)
                    .productId(itemRequest.getProductId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .notes(itemRequest.getNotes())
                    .build();
            })
            .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
    }

    private void addStatusHistory(Order order, Order.OrderStatus status, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
            .order(order)
            .status(status)
            .notes(notes)
            .build();

        orderStatusHistoryRepository.save(history);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = orderItemRepository.findByOrderId(order.getId())
            .stream()
            .map(this::mapItemToResponse)
            .toList();

        List<OrderStatusHistoryResponse> historyResponses = orderStatusHistoryRepository
            .findByOrderIdOrderByCreatedAtAsc(order.getId())
            .stream()
            .map(this::mapHistoryToResponse)
            .toList();

        RestaurantResponse restaurant = restaurantServiceClient.getRestaurant(order.getRestaurantId());

        return OrderResponse.builder()
            .id(order.getId())
            .userId(order.getUserId())
            .restaurantId(order.getRestaurantId())
            .restaurantName(restaurant.getName())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .deliveryAddress(order.getDeliveryAddress())
            .customerNotes(order.getCustomerNotes())
            .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
            .actualDeliveryTime(order.getActualDeliveryTime())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .items(itemResponses)
            .statusHistory(historyResponses)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .notes(item.getNotes())
            .build();
    }

    private OrderStatusHistoryResponse mapHistoryToResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
            .id(history.getId())
            .status(history.getStatus())
            .notes(history.getNotes())
            .createdAt(history.getCreatedAt())
            .build();
    }
}
