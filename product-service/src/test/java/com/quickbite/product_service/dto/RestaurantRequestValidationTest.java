package com.quickbite.product_service.dto;

import com.quickbite.product_service.constants.TestConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestaurantRequestValidationTest {

    private final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldFail_whenOwnerIdIsNull() {
        RestaurantRequest request = RestaurantRequest.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertTrue(
            violations.stream()
                .anyMatch(v ->
                    v.getPropertyPath().toString().equals("ownerId")
                )
        );
    }

    @Test
    void shouldFail_whenOwnerIdIsInvalid() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.INVALID_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertTrue(
            violations.stream()
                .anyMatch(v ->
                    v.getPropertyPath().toString().equals("ownerId")
                )
        );
    }

    @Test
    void shouldFail_whenNameIsBlank() {
        RestaurantRequest request = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.BLANK_RESTAURANT_NAME)
            .build();

        Set<ConstraintViolation<RestaurantRequest>> violations =
            validator.validate(request);

        assertTrue(
            violations.stream()
                .anyMatch(v ->
                    v.getPropertyPath().toString().equals("name")
                )
        );
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

        assertTrue(
            violations.stream()
                .anyMatch(v ->
                    v.getPropertyPath().toString().equals("email")
                )
        );
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

        assertTrue(violations.stream()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("phone")
            )
        );
    }

    @Test
    void shouldPass_whenRequestIsInvalid() {
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
