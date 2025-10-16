package com.quickbite.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class RestaurantRequest {
    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private Map<String, Object> address;
    private String phone;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    private String cuisineType;
    private Boolean isActive = true;
    private Map<String, Object> openingHours;
    private String deliveryTimeRange;
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;
}
