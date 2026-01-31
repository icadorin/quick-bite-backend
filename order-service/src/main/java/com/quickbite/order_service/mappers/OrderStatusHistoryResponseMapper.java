package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dtos.OrderStatusHistoryResponse;
import com.quickbite.order_service.entity.OrderStatusHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderStatusHistoryResponseMapper {

    OrderStatusHistoryResponse toResponse(OrderStatusHistory history);

    List<OrderStatusHistoryResponse> toResponseList(List<OrderStatusHistory> history);
}
