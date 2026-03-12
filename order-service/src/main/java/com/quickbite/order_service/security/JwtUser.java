package com.quickbite.order_service.security;

import com.quickbite.core.security.UserRole;

public record JwtUser(
    Long id,
    Long restaurantId,
    UserRole role
) {}
