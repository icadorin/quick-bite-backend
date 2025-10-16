package com.quickbite.product_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ProductResponse {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private BigDecimal costPrice;
    private String imageUrl;
    private Map<String, Object> ingredients;
    private Map<String, Object> allergens;
    private Boolean isAvailable;
    private Boolean isFeatured;
    private Integer preparationTime;
    private Integer calories;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
