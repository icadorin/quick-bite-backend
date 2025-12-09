package com.quickbite.product_service.mapper;

import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductPatchMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(
        target = "description",
        expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)"
    )
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product entity);
}
