package com.quickbite.auth_service.security;

import com.quickbite.auth_service.repository.UserRepository;
import com.quickbite.auth_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository repository;

    public boolean canManageUser(Long userId) {

        if (SecurityUtils.hasRole("ADMIN")) {
            return true;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(userId);
    }
}
