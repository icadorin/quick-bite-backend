package com.quickbite.product_service.mapper;

import com.quickbite.core.mapper.config.PatchMapperConfig;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.entity.Product;
import org.mapstruct.*;

@Mapper(config = PatchMapperConfig.class)
public interface ProductPatchMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", expression = "java(request.getName() != null ? request.getName().trim() : null)")
    @Mapping(
        target = "description",
        expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)"
    )
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "comparePrice",  ignore = true)
    @Mapping(target = "costPrice", ignore = true)
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "ingredients", source = "ingredients")
    @Mapping(target = "allergens", source = "allergens")
    @Mapping(target = "isAvailable", source = "isAvailable")
    @Mapping(target = "isFeatured", source = "isFeatured")
    @Mapping(target = "preparationTime", source = "preparationTime")
    @Mapping(target = "calories", source = "calories")
    @Mapping(target = "sortOrder", source = "sortOrder")
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product entity);
}
