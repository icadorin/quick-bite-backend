package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dto.DeliveryAddressRequest;
import com.quickbite.order_service.model.DeliveryAddress;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = OrderItemCreateMapper.class
)
public interface OrderCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "estimatedDeliveryTime", ignore = true)
    @Mapping(target = "actualDeliveryTime", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequest request);

    default DeliveryAddress map(DeliveryAddressRequest request) {
        if (request == null) return null;

        return DeliveryAddress.builder()
            .state(request.getState())
            .number(request.getNumber())
            .city(request.getCity())
            .state(request.getState())
            .zipCode(request.getZipCode())
            .complement(request.getComplement())
            .build();
    }
}
