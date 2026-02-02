package com.quickbite.product_service.constants;

public final class SecuredEndPoints {

    private SecuredEndPoints() {}

    public static final String[] ADMIN = {
        "/admin/**"
    };

    public static final String[] RESTAURANT_MANAGEMENT = {
        "/api/categories/**",
        "/api/products/**",
        "/api/restaurants/**"
    };
}
