package com.quickbite.order_service.security;

public record AuthContext(
    Long userId,
    String role
) {}
