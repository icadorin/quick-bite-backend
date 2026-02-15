package com.quickbite.product_service.security;

import com.quickbite.core.security.UserRole;
import com.quickbite.product_service.repository.RestaurantRepository;
import com.quickbite.product_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantSecurity {

    private final RestaurantRepository repository;

    public boolean canManageRestaurant(Long restaurantId) {

        if (SecurityUtils.hasRole(UserRole.ADMIN)) {
            return true;
        }

        Long userId = SecurityUtils.getCurrentUserId();

        return repository.existsByIdAndOwnerId(restaurantId, userId);
    }
}
