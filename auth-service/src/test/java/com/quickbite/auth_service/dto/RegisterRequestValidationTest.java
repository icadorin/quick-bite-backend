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

public class RegisterRequestValidationTest {

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
    void shouldFail_whenFullNameIsBlank() {
        RegisterRequest request = RegisterRequest.builder()
            .fullName("")
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "fullName");
    }

    @Test
    void shouldFail_whenEmailIsInvalid() {
        RegisterRequest request = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.INVALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "email");
    }

    @Test
    void shouldFail_whenPasswordIsTooShort() {
        RegisterRequest request = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.SHORT_PASSWORD)
            .build();

        Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "password");
    }

    @Test
    void shouldFail_whenPhoneIsInvalid() {
        RegisterRequest request = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .phone(TestConstants.INVALID_PHONE)
            .build();

        Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "phone");
    }

    @Test
    void shouldPass_whenRequestIsValid() {
        RegisterRequest request = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .phone(TestConstants.VALID_PHONE)
            .build();

        assertTrue(validator.validate(request).isEmpty());
    }
}
