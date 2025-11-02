package com.quickbite.order_service.dtos;

import com.quickbite.order_service.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String customerNotes;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
