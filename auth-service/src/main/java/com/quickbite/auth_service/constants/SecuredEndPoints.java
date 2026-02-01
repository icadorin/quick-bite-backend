package com.quickbite.auth_service.constants;

public final class SecuredEndPoints {

    public static final String[] ADMIN = {
        "/admin/**"
    };

    public static final String[] RESTAURANT = {
        "/restaurant/**"
    };

    private SecuredEndPoints() {}
}
