package com.quickbite.order_service.constants;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String API = "/api/v1";

    public static final String ORDERS = API + "/orders";

    public static final String BY_ID = "/{id}";
    public static final String STATUS = "/status";
    public static final String CANCEL = "/cancel";

    public static final String RESTAURANT = "/restaurant";
    public static final String BY_RESTAURANT_ID = "/{restaurantId}";
}
