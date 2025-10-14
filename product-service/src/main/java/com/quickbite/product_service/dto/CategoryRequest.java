package com.quickbite.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String  imageUrl;
    private Integer sortOrder = 0;
    private Boolean isActive = true;
}
