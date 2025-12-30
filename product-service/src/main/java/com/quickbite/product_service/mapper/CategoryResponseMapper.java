package com.quickbite.product_service.mapper;

import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryResponseMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}
