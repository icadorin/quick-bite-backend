package com.quickbite.order_service.constants;

import java.math.BigDecimal;

public class TestConstants {

    private TestConstants() {}

    public static final Long VALID_ORDER_ID = 1L;
    public static final Long VALID_USER_ID = 1L;
    public static final Long SECOND_USER_ID = 2L;
    public static final Long THIRD_USER_ID = 3L;
    public static final Long CUSTOMER_USER_ID = 99L;

    public static final Long VALID_RESTAURANT_ID = 10L;
    public static final Long OTHER_RESTAURANT_ID = 20L;

    public static final Long VALID_PRODUCT_ID = 1L;
    public static final Long SECOND_PRODUCT_ID = 2L;

    public static final Long INVALID_ID = -1L;
    public static final Long NON_EXISTENT_ID = 999L;

    public static final String VALID_PRODUCT_NAME = "Burger";
    public static final String GENERIC_PRODUCT_NAME = "Any";

    public static final String VALID_STREET = "Street";
    public static final String VALID_NUMBER = "183";
    public static final String VALID_CITY = "Miami";
    public static final String VALID_STATE = "Florida";
    public static final String VALID_ZIP_CODE = "44505";
    public static final String VALID_COMPLEMENT = "Apt 1";

    public static final String CONTROLLER_STREET = "Z street";
    public static final String CONTROLLER_NUMBER = "445";
    public static final String CONTROLLER_CITY = "Miami";
    public static final String CONTROLLER_STATE = "Florida";
    public static final String CONTROLLER_ZIP = "";
    public static final String CONTROLLER_COMPLEMENT = "Z complement";

    public static final Integer SINGLE_QUANTITY = 1;
    public static final Integer DOUBLE_QUANTITY = 2;

    public static final String ANY_PRODUCT_NAME = "Any";

    public static final BigDecimal VALID_PRODUCT_PRICE =
        BigDecimal.valueOf(10);

    public static final BigDecimal VALID_TOTAL_AMOUNT =
        BigDecimal.valueOf(50);

    public static final BigDecimal ITEM_PRICE =
        BigDecimal.valueOf(15);

    public static final BigDecimal FIRST_ITEM_TOTAL =
        BigDecimal.valueOf(20);

    public static final BigDecimal SECOND_ITEM_TOTAL =
        BigDecimal.valueOf(30);

    public static final BigDecimal EXPECTED_TOTAL_PRICE =
        BigDecimal.valueOf(30);

    public static final BigDecimal RECALCULATED_TOTAL =
        BigDecimal.valueOf(50);

    public static final String VALID_REASON = "ok";
    public static final String CONFIRMED_REASON = "confirmed";
    public static final String INVALID_REASON = "invalid";
}