package com.quickbite.order_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private Long restaurantId;
    private String deliveryAddress;
    private String customerNotes;
    private String paymentMethod;
    private List<OrderItemRequest> items;
}
