package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dtos.OrderItemResponse;
import com.quickbite.order_service.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemResponseMapper {

    OrderItemResponse toResponse(OrderItem item);

    List<OrderItemResponse> toResponseList(List<OrderItem> items);
}
