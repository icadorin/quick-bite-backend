package com.quickbite.product_service.dto.filter;

public record RestaurantFilter(
    String name,
    Long ownerId,
    String cuisineType,
    Double minRating,
    Boolean isActive
) {}
