package com.quickbite.product_service.dto.filter;

import java.math.BigDecimal;

public record ProductFilter(
    Long restaurantId,
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice
) {}