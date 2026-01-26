package com.quickbite.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull(message = "Restaurant ID is required")
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;

    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Product price must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 integer digits and 2" +
        "fractions digits")
    private BigDecimal price;

    @Positive(message = "Cost price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Compare price must have up to 10 integer digits and" +
        "2 fraction digits")
    private BigDecimal comparePrice;

    @NotNull(message = "Cost price is required")
    @Positive(message = "Cost price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Cost price must have up to 10 integer digits and 2" +
        "fraction digits")
    private BigDecimal costPrice;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private Map<String, Object> ingredients;
    private Map<String, Object> allergens;

    @Builder.Default
    private Boolean isAvailable = true;

    @Builder.Default
    private Boolean isFeatured = false;

    @PositiveOrZero(message = "Preparation time must be zero or positive")
    private Integer preparationTime;

    @PositiveOrZero(message = "Calories must be zero or positive")
    private Integer calories;

    @PositiveOrZero(message = "Sort order must be zero or positive")
    @Builder.Default
    private Integer sortOrder = 0;
}
