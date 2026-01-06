package com.quickbite.auth_service.mapper;

import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "CUSTOMER")
    @Mapping(target = "status", constant = "ACTIVE")
    User toEntity(RegisterRequest request);
}
