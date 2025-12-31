package com.quickbite.product_service.constants;

import java.math.BigDecimal;

public class TestConstants {

    public static final Long VALID_CATEGORY_ID = 1L;
    public static final Long VALID_PRODUCT_ID = 1L;
    public static final Long VALID_RESTAURANT_ID = 1L;
    public static final Long VALID_RESTAURANT_ID_2 = 2L;
    public static final Long VALID_OWNER_ID = 1L;
    public static final Long INVALID_ID = -1L;
    public static final Long NON_EXISTENT_ID = 999L;

    public static final String VALID_CATEGORY_NAME = "Test Category";
    public static final String UPDATED_CATEGORY_NAME = "Updated Category";

    public static final String VALID_PRODUCT_NAME = "Teste Product";
    public static final String UPDATED_PRODUCT_NAME = "Updated Product";

    public static final String VALID_RESTAURANT_NAME = "Test Restaurant";
    public static final String VALID_RESTAURANT_NAME_2 = "Another Italian Restaurant";
    public static final String BLANK_RESTAURANT_NAME = " ";
    public static final String UPDATED_RESTAURANT_NAME = "Updated Restaurant";

    public static final String VALID_DESCRIPTION = "Test Description";
    public static final String UPDATED_DESCRIPTION = "Updated Description";
    public static final String LONG_DESCRIPTION = "A".repeat(1001);

    public static final String VALID_IMAGE_URL = "http://test.com/image.jpg";
    public static final String VALID_LOGO_URL = "http://test.com/logo.jpg";
    public static final String VALID_BANNER_URL = "http://test.com/banner.jpg";

    public static final Double VALID_PRICE = 29.99;
    public static final Double VALID_COMPARE_PRICE = 39.99;
    public static final Double VALID_COST_PRICE = 15.99;

    public static final BigDecimal MIN_PRICE = new BigDecimal("10.00");
    public static final BigDecimal MAX_PRICE = new BigDecimal("50.00");
    public static final BigDecimal INVALID_MIN_PRICE = new BigDecimal("60.00");
    public static final Double NEGATIVE_PRICE = -10.0;

    public static final Integer VALID_PREPARATION_TIME = 30;
    public static final Integer VALID_CALORIES = 500;
    public static final Integer VALID_SORT_ORDER = 1;
    public static final Integer ZERO_SORT_ORDER = 0;
    public static final Integer NEGATIVE_SORT_ORDER = -1;

    public static final Long NO_ASSOCIATED_PRODUCTS = 0L;
    public static final Long HAS_ASSOCIATED_PRODUCTS = 2L;

    public static final String VALID_EMAIL = "restaurant@test.com";
    public static final String VALID_PHONE = "+5511999999999";

    public static final String INVALID_EMAIL = "invalid-email";
    public static final String INVALID_PHONE = "0123";

    public static final String LONG_EMAIL = "a".repeat(256) + "@test.com";

    public static final Double VALID_RATING = 4.5;
    public static final Double INVALID_RATING = 6.0;

    public static final String VALID_CUISINE_TYPE = "Italian";
    public static final String VALID_CUISINE_TYPE_2 = "Brazilian";
    public static final String VALID_CUISINE_TYPE_3 = "Japanese";

    public static final String VALID_CUISINE_100 = "A".repeat(100);
    public static final String INVALID_CUISINE_TYPE = "A".repeat(101);

    public static final String LONG_NAME = "A".repeat(256);

    public static final String VALID_SEARCH_TERM = "test";
    public static final String SEARCH_TERM_SHORT = "a";
    public static final String SEARCH_TERM_CHEESE = "cheese";

    public static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found with id: ";
    public static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found with id: ";
    public static final String RESTAURANT_NOT_FOUND_MESSAGE = "Restaurant not found with id: ";

    public static final String CATEGORY_ALREADY_EXISTS_MESSAGE =
        "Category with name '%s' already exists";

    public static final String RESTAURANT_ALREADY_EXISTS_MESSAGE =
        "Restaurant with name '%s' already exists for this owner";

    public static final String CATEGORY_HAS_PRODUCTS_MESSAGE =
        "Cannot delete category with associated products";

    public static final String INVALID_PRICE_RANGE_MESSAGE =
        "minPrice must be less than or equal to maxPrice";

    public static final String INVALID_RATING_MESSAGE =
        "Minimum rating must be between 0 and 5";

    public static final String INVALID_RESTAURANT_ID_MESSAGE =
        "Invalid restaurant ID";

    public static final String SEARCH_TERM_TOO_SHORT_MESSAGE =
        "Search term must be at least 2 characters long";

    public static final String PRODUCT_NAME_REQUIRED_MESSAGE =
        "Product name is required";

    public static final String CATEGORY_NAME_REQUIRED_MESSAGE =
        "Category name is required";

    public static final String RESTAURANT_NAME_REQUIRED_MESSAGE =
        "Restaurant name is required";

    public static final String NAME_TOO_LONG_MESSAGE =
        "Category name must not exceed 100 characters";

    public static final String CUISINE_TOO_LONG_MESSAGE =
        "Cuisine type must not exceed 100 characters";

    public static final String PRICE_REQUIRED_MESSAGE =
        "Product price is required";

    public static final String PRICE_MUST_GREATER_ZERO =
        "Product price must be greater than zero";

    public static final String RESTAURANT_ID_REQUIRED_MESSAGE =
        "Valid restaurant ID is required";

    public static final String OWNER_ID_REQUIRED_MESSAGE =
        "Valid owner ID is required";

    public static final String INVALID_EMAIL_MESSAGE =
        "Email must be valid";

    public static final String INVALID_PHONE_MESSAGE =
        "Phone number must be valid";

    public static final String COMPARE_PRICE_MUST_BE_GREATER_THAN_CURRENT_PRICE =
        "Compare price should be greater than current price";
}
