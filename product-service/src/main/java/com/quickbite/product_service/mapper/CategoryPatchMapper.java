package com.quickbite.product_service.mapper;

import com.quickbite.core.mapper.config.PatchMapperConfig;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.entity.Category;
import org.mapstruct.*;

@Mapper(config = PatchMapperConfig.class)
public interface CategoryPatchMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(
        target = "name",
        expression = "java(request.getName() != null ? request.getName().trim() : null)"
    )
    @Mapping(
        target = "description",
        expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)"
    )
    void updateCategoryFromRequest(CategoryRequest request, @MappingTarget Category category);
}
