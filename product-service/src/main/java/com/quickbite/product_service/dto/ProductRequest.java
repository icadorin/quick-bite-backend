package com.quickbite.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotNull(message = "Resturant ID is required")
    private Long restaurantId;

    private Long categoryId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal comparePrice;
    private BigDecimal costPrice;
    private String imageUrl;
    private List<String> ingredients;
    private List<String> allergens;
    private Boolean isAvailable = true;
    private Boolean isFeatured = false;
    private Integer preparationTime;
    private Integer calories;
    private Integer sortOrder = 0;
}
