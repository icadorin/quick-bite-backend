package com.quickbite.product_service.mapper;

import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Restaurant toEntity(RestaurantRequest request);
}
