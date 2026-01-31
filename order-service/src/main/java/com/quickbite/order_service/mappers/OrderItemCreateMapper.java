package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dtos.OrderItemRequest;
import com.quickbite.order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    OrderItem toEntity(OrderItemRequest request);
}
