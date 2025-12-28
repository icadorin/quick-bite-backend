package com.quickbite.order_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull
    @Positive
    private Long restaurantId;

    @NotBlank
    private String deliveryAddress;
    private String customerNotes;
    private String paymentMethod;

    @NotEmpty
    private List<OrderItemRequest> items;
}
