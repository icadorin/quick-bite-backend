package com.quickbite.auth_service.dto.filter;

import com.quickbite.auth_service.entity.User;
import com.quickbite.core.security.UserRole;

public record UserFilter(
    String email,
    UserRole role,
    User.UserStatus status
) {}
