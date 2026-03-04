package com.quickbite.order_service.dtos;

import com.quickbite.order_service.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
