package com.quickbite.order_service.dto;

import lombok.Data;

@Data
public class RestaurantResponse {

    private Long id;
    private String name;
    private String description;
    private String phone;
    private String email;
    private Boolean isActive;
    private String deliveryTimeRange;
}
