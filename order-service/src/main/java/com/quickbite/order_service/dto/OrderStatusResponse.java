package com.quickbite.order_service.dto;

public record OrderStatusResponse(
    long pending,
    long preparing,
    long delivered,
    long cancelled
) {}
