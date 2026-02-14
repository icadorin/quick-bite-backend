package com.quickbite.auth_service.mapper;

import com.quickbite.auth_service.dto.UpdateUserRequest;
import com.quickbite.auth_service.entity.User;
import com.quickbite.core.mapper.config.PatchMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = PatchMapperConfig.class)
public interface UserPatchMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(
        target = "fullName",
        expression = "java(request.fullName() != null ? request.fullName().trim() : null)"
    )
    void updateUserFromRequest(UpdateUserRequest request,
                               @MappingTarget User user);
}
