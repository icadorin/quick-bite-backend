package com.quickbite.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class RestaurantRequest {

    @NotNull(message = "Owner ID is required")
    @Positive(message = "Owner ID must be positive")
    private Long ownerId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Map<String, Object> address;

    @Pattern(regexp = "^[\\+]?[1-9][\\d]{0,15}$", message = "Phone number must be valid")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;

    @Size(max = 100, message = "Cusine type must not exceed 100 characters")
    private String cuisineType;

    private Boolean isActive = true;

    private Map<String, Object> openingHours;

    @Size(max = 20, message = "Delivery time range must not exceed 20 characters")
    private String deliveryTimeRange;

    @PositiveOrZero(message = "Minimum amount must be zero or positive")
    @Digits(integer = 10, fraction = 2, message = "Minimum order amount must have up to" +
        "10 integer digits and 2 fraction digits")
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;
}
