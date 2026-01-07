package com.quickbite.product_service.support;


import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationTestHelper {

    private ValidationTestHelper() {}

    public static <T> void assertHasViolationOnField(
        Set<ConstraintViolation<T>> violations,
        String field
    ) {
        assertTrue(
            violations.stream().anyMatch(
                v -> v.getPropertyPath().toString().equals(field)
            ),
            String.format("Expected validation error on field: %s", field)
        );
    }
}
