package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dto.DeliveryAddress;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "estimatedDeliveryTime", ignore = true)
    @Mapping(target = "actualDeliveryTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequest request);

    default DeliveryAddress map(String value) {
        if (value == null) return null;

        DeliveryAddress address = new DeliveryAddress();
        address.setStreet(value);

        return address;
    }
}
