package com.quickbite.product_service.mapper;

import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RestaurantResponseMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "ownerId", target = "ownerId")
    RestaurantResponse toResponse(Restaurant restaurant);

    List<RestaurantResponse> toResponseList(List<Restaurant> restaurants);
}
