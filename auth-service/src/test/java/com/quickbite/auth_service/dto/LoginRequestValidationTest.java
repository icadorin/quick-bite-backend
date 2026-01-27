package com.quickbite.auth_service.dto;


import com.quickbite.auth_service.constants.TestConstants;
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

public class LoginRequestValidationTest {

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
    void shouldFail_whenEmailIsBlank() {
        LoginRequest request = LoginRequest.builder()
            .email("")
            .password(TestConstants.VALID_PASSWORD)
            .build();

        Set<ConstraintViolation<LoginRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "email");
    }

    @Test
    void shouldFail_whenEmailIsInvalid() {
        LoginRequest request = LoginRequest.builder()
            .email(TestConstants.INVALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        Set<ConstraintViolation<LoginRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "email");
    }

    @Test
    void shouldFail_whenPasswordIsBlank() {
        LoginRequest request = LoginRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password("")
            .build();

        Set<ConstraintViolation<LoginRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "password");
    }

    @Test
    void shouldPass_whenRequestIdValid() {
        LoginRequest request = LoginRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        Set<ConstraintViolation<LoginRequest>> violations =
            validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
