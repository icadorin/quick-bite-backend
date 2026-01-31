package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dtos.OrderResponse;
import com.quickbite.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {
        OrderItemResponseMapper.class,
        OrderStatusHistoryResponseMapper.class
    }
)
public interface OrderResponseMapper {

    @Mapping(target = "restaurantName", ignore = true)
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);
}
