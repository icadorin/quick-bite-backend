package com.quickbite.product_service.security;

import com.quickbite.product_service.repository.RestaurantRepository;
import com.quickbite.product_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantSecurity {

    private final RestaurantRepository repository;

    public boolean canManageRestaurant(Long restaurantId) {

        String email = SecurityUtils.getCurrentUserEmail();

        if (SecurityUtils.hasRole("ADMIN")) {
            return true;
        }

        return repository.existsByIdAndOwnerEmail(restaurantId, email);
    }
}
