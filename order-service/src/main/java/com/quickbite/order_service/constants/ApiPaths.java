package com.quickbite.order_service.constants;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String API_V1 = "/api/v1";

    public static final String ORDERS = API_V1 + "/orders";

    public static final String ORDER_ID = "/{id}";
    public static final String RESTAURANT_ID = "/{restaurantId}";

    public static final String STATUS = "/status";
    public static final String CANCEL = "/cancel";

    public static final String RESTAURANT = "/restaurant";
    public static final String BY_RESTAURANT = RESTAURANT + RESTAURANT_ID;
}
