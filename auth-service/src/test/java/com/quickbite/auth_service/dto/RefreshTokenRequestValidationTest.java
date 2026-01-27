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

public class RefreshTokenRequestValidationTest {

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
    void shouldFail_whenRefreshTokenIsBlank() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken("")
            .build();

        Set<ConstraintViolation<RefreshTokenRequest>> violations =
            validator.validate(request);

        assertHasViolationOnField(violations, "refreshToken");
    }

    @Test
    void shouldPass_whenRequestIsValid() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken(TestConstants.VALID_REFRESH_TOKEN)
            .build();

        assertTrue(validator.validate(request).isEmpty());
    }
}
