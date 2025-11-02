package com.quickbite.order_service.dtos;

import com.quickbite.order_service.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    private Order.OrderStatus status;
    private String notes;
}
