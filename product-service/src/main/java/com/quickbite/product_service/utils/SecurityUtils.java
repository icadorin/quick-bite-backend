package com.quickbite.product_service.utils;

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

    public static String getCurrentUserEmail() {
        return getAuthentication().getName();
    }

    public static Long getCurrentUserId() {
        return Long.valueOf(getAuthentication().getName());
    }

    public static boolean hasRole(String role) {
        return getAuthentication()
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
