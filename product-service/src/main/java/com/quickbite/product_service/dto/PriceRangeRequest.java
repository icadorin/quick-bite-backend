package com.quickbite.product_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PriceRangeRequest (
    @NotNull @PositiveOrZero BigDecimal minPrice,
    @NotNull @PositiveOrZero BigDecimal maxPrice
) {}
