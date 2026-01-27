package com.quickbite.auth_service.mapper;

import com.quickbite.auth_service.dto.UserResponse;
import com.quickbite.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
