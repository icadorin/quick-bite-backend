package com.quickbite.product_service.mapper;

import com.quickbite.core.mapper.config.PatchMapperConfig;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.entity.Product;
import org.mapstruct.*;

@Mapper(config = PatchMapperConfig.class)
public interface ProductPatchMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(
        target = "description",
        expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)"
    )
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product entity);
}
