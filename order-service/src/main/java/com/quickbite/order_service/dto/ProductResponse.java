package com.quickbite.order_service.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;
    private Integer preparationTime;
}
