package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {
        OrderItemResponseMapper.class,
        OrderStatusHistoryResponseMapper.class,
        DeliveryAddressMapper.class
    }
)
public interface OrderResponseMapper {

    @Mapping(target = "restaurantName", ignore = true)
    OrderResponse toResponse(Order order);
}
