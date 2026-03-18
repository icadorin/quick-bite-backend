package com.quickbite.order_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAddress {

    private String street;
    private String number;
    private String city;
    private String state;
    private String zipCode;
    private String complement;
}
