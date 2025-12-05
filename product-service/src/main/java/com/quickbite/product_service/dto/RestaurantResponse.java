package com.quickbite.product_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class RestaurantResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private Map<String, Object> address;
    private String phone;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    private String cuisineType;
    private Boolean isActive;
    private Map<String, Object> openingHours;
    private String deliveryTimeRange;
    private BigDecimal minimumOrderAmount;
    private Double rating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
