package com.quickbite.product_service.constants;

public class TestConstants {

    private static final Long VALID_CATEGORY_ID = 1L;
    private static final Long VALID_PRODUCT_ID = 1L;
    private static final Long VALID_RESTAURANT_ID = 1L;
    private static final Long VALID_OWNER_ID = 1L;
    private static final Long INVALID_ID = -1L;
    private static final Long NON_EXISTENT_ID = 999L;

    private static final String VALID_CATEGORY_NAME = "Test Category";
    private static final String VALID_PRODUCT_NAME = "Teste Product";
    private static final String VALID_RESTAURANT_NAME = "Test Restaurant";
    private static final String UPDATED_CATEGORY_NAME = "Updated Category";
    private static final String UPDATED_PRODUCT_NAME = "Updated Product";
    private static final String UPDATED_RESTAURANT_NAME = "Updated Restaurant";

    private static final String VALID_DESCRIPTION = "Test Description";
    private static final String UPDATED_DESCRIPTION = "Updated Description";

    public static final String VALID_IMAGE_URL = "http://test.com/image.jpg";
    public static final String VALID_LOGO_URL = "http://test.com/logo.jpg";
    public static final String VALID_BANNER_URL = "http://test.com/banner.jpg";

    public static final Double VALID_PRICE = 29.99;
    public static final Double VALID_COMPARE_PRICE = 39.99;
    public static final Double VALID_COST_PRICE = 15.99;
    public static final Double MIN_PRICE = 10.0;
    public static final Double MAX_PRICE = 50.0;
    public static final Double INVALID_MIN_PRICE = 60.0;
    public static final Double NEGATIVE_PRICE = -10.0;

    public static final Integer VALID_PREPARATION_TIME = 30;
    public static final Integer VALID_CALORIES = 500;
    public static final Integer VALID_SORT_ORDER = 1;

    public static final String VALID_EMAIL = "restaurant@test.com";
    public static final String VALID_PHONE = "+5511999999999";
    public static final String INVALID_EMAIL = "invalid-email";
    public static final String INVALID_PHONE = "123";

    public static final Double VALID_RATING = 4.5;
    public static final Double INVALID_RATING = 6.0;

    public static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found with id: ";
    public static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found with id: ";
    public static final String RESTAURANT_NOT_FOUND_MESSSAGE = "Restaurant not found with id: "
    public static final String CATEGORY_ALREADY_EXISTS_MESSAGE = "Category with name  '%s' already exists"
    public static final String RESTAURANT_ALREADY_EXISTS_MESSAGE = "Restaurant with name  '%s' already exists"
}
