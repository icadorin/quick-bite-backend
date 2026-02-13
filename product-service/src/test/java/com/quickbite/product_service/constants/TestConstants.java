package com.quickbite.product_service.constants;

import java.math.BigDecimal;

public class TestConstants {

    public static final Long VALID_CATEGORY_ID = 1L;
    public static final Long VALID_PRODUCT_ID = 1L;
    public static final Long VALID_RESTAURANT_ID = 1L;
    public static final Long VALID_OWNER_ID = 1L;
    public static final Long INVALID_ID = -1L;
    public static final Long NON_EXISTENT_ID = 999L;

    public static final String VALID_CATEGORY_NAME = "Test Category";
    public static final String UPDATED_CATEGORY_NAME = "Updated Category";

    public static final String VALID_PRODUCT_NAME = "Teste Product";

    public static final String VALID_RESTAURANT_NAME = "Test Restaurant";
    public static final String BLANK_RESTAURANT_NAME = " ";

    public static final String VALID_DESCRIPTION = "Test Description";

    public static final String VALID_IMAGE_URL = "http://test.com/image.jpg";

    public static final Double VALID_PRICE = 29.99;
    public static final Double VALID_COST_PRICE = 15.99;
    public static final BigDecimal INVALID_PRICE_HIGH = new BigDecimal("100");
    public static final BigDecimal INVALID_COMPARE_PRICE_LOW = new BigDecimal("50");

    public static final Integer VALID_SORT_ORDER = 1;
    public static final Integer NEGATIVE_SORT_ORDER = -1;

    public static final Long NO_ASSOCIATED_PRODUCTS = 0L;
    public static final Long HAS_ASSOCIATED_PRODUCTS = 2L;

    public static final String VALID_EMAIL = "restaurant@test.com";
    public static final String VALID_PHONE = "+5511999999999";

    public static final String INVALID_EMAIL = "invalid-email";
    public static final String INVALID_PHONE = "0123";

    public static final String VALID_CUISINE_TYPE = "Italian";

    public static final String SEARCH_TERM_PIZZA = "Pizza";

    public static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found with id: ";
    public static final String RESTAURANT_NOT_FOUND_MESSAGE = "Restaurant not found with id: ";
    public static final String TEST_CATEGORY_FILTER_NAME = "Food";

    public static final String ACTIVE_CATEGORY_NAME = "ActiveCategory";
    public static final String INACTIVE_CATEGORY_NAME = "InactiveCategory";

    public static final String CATEGORY_NAME_WITH_SPACES = " Pizza ";
    public static final String CATEGORY_NAME_TRIMMED = "Pizza";

    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "20";
    public static final String DEFAULT_SORT = "name,asc";

    public static final String CATEGORY_ALREADY_EXISTS_MESSAGE =
        "Category with name '%s' already exists";

    public static final String CATEGORY_NAME_REQUIRED_MESSAGE =
        "Category name is required";

    public static final String COMPARE_PRICE_MUST_BE_GREATER_THAN_CURRENT_PRICE =
        "Compare price should be greater than current price";

    public static final String INVALID_OWNER_ID_MESSAGE =
        "Invalid owner ID: -1";

    public static final String RESTAURANT_NAME_ALREADY_EXISTS_MESSAGE =
        "Restaurant name already exists for this owner";
}
