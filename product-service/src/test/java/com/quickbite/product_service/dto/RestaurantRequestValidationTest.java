package com.quickbite.product_service.dto;

import com.quickbite.product_service.constants.TestConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.quickbite.core.support.ValidationTestHelper.assertHasViolationOnField;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestaurantRequestValidationTest {

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
    void shouldFail_whenOwnerIdIsNull() {
        RestaurantRequest request = RestaurantRequest.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "ownerId");
    }

    @Test
    void shouldFail_whenOwnerIdIsInvalid() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.INVALID_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "ownerId");
    }

    @Test
    void shouldFail_whenNameIsBlank() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.BLANK_RESTAURANT_NAME)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "name");
    }

    @Test
    void shouldFail_whenEmailIsInvalid() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .email(TestConstants.INVALID_EMAIL)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "email");
    }

    @Test
    void shouldFail_whenPhoneIsInvalid() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .phone(TestConstants.INVALID_PHONE)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "phone");
    }

    @Test
    void shouldPass_whenRequestIsValid() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .email(TestConstants.VALID_EMAIL)
            .phone(TestConstants.VALID_PHONE)
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
