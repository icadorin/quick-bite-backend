package com.quickbite.order_service.dto;

import com.quickbite.order_service.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {

    private Long id;
    private Order.OrderStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
