package com.quickbite.product_service.dto;

import com.quickbite.product_service.constants.TestConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        ProductRequest request = ProductRequest.builder()
            .name("")
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenPriceIsNull() {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassWhenRequestIsValid() {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
