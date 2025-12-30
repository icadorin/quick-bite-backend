package com.quickbite.product_service.dto;


import com.quickbite.product_service.constants.TestConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoryRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFail_whenNameIsBlank() {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(TestConstants.BLANK_RESTAURANT_NAME);

        Set<ConstraintViolation<CategoryRequest>> violations =
            validator.validate(categoryRequest);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFail_whenNameIsTooShort() {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(TestConstants.SHORT_NAME);

        Set<ConstraintViolation<CategoryRequest>> violations =
            validator.validate(categoryRequest);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFail_whenSortOrderIsNegative() {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(TestConstants.VALID_CATEGORY_NAME);
        categoryRequest.setSortOrder(TestConstants.NEGATIVE_SORT_ORDER);

        Set<ConstraintViolation<CategoryRequest>> violations =
            validator.validate(categoryRequest);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPass_whenRequestIsValid() {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(TestConstants.VALID_CATEGORY_NAME);
        categoryRequest.setSortOrder(TestConstants.VALID_SORT_ORDER);

        Set<ConstraintViolation<CategoryRequest>> violations =
            validator.validate(categoryRequest);

        assertTrue(violations.isEmpty());
    }
}
