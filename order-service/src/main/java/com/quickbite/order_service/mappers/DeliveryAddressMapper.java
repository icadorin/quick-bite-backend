package com.quickbite.order_service.mappers;

import com.quickbite.order_service.dto.DeliveryAddressRequest;
import com.quickbite.order_service.model.DeliveryAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryAddressMapper {

    DeliveryAddressRequest toRequest(DeliveryAddress value);

    DeliveryAddress toModel(DeliveryAddressRequest request);
}
