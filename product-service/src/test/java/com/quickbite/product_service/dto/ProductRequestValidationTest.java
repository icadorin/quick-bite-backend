package com.quickbite.product_service.dto;

import com.quickbite.product_service.constants.TestConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Set;

import static com.quickbite.core.support.ValidationTestHelper.assertHasViolationOnField;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductRequestValidationTest {

    private static Validator validator;
    private static ValidatorFactory factory;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldFail_whenNameIsBlank() {
        ProductRequest request = ProductRequest.builder()
            .name("")
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        Set<ConstraintViolation<ProductRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "name");
    }

    @Test
    void shouldFail_whenPriceIsNull() {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        Set<ConstraintViolation<ProductRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "price");
    }

    @Test
    void shouldPass_whenRequestIsValid() {
        ProductRequest request = ProductRequest.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .build();

        Set<ConstraintViolation<ProductRequest>> violations =
            validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
