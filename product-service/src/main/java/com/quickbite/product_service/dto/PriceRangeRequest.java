package com.quickbite.product_service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PriceRangeRequest (
    @NotNull @PositiveOrZero BigDecimal minPrice,
    @NotNull @PositiveOrZero BigDecimal maxPrice
) {
    @AssertTrue(message = "minPrice must be less than or equal to maxPrice")
    public boolean isValidRange() {
        return minPrice.compareTo(maxPrice) <= 0;
    }
}
