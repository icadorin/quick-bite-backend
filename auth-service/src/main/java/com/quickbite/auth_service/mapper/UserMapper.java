package com.quickbite.auth_service.mapper;

import com.quickbite.auth_service.dto.UserResponse;
import com.quickbite.auth_service.entity.User;

public class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getFullName())
            .role(user.getRole().name())
            .build();
    }
}
