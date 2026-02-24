package com.quickbite.product_service.dto.filter;

import java.math.BigDecimal;

public record ProductFilter(
    Long restaurantId,
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean onlyAvailable
) {
    public ProductFilter {
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                "minPrice must be <= maxPrice"
            );
        }
    }
}