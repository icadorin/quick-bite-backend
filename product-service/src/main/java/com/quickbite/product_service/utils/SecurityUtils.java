package com.quickbite.product_service.utils;

import com.quickbite.core.security.UserRole;
import com.quickbite.product_service.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    public static Authentication getAuthentication() {

        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        return auth;
    }

    public static AuthenticatedUser getCurrentUser() {
        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof AuthenticatedUser user) {
            return user;
        }

        throw new IllegalStateException("Invalid authentication principal");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public static boolean hasRole(UserRole role) {
        return getAuthentication()
            .getAuthorities()
            .stream()
            .anyMatch(
                a -> a.getAuthority().equals(role.getAuthority())
            );
    }
}
