package com.quickbite.auth_service.constants;

public final class PublicEndPoints {

    private PublicEndPoints() {}

    public static final String AUTH = "/auth/**";
    public static final String API_AUTH = "/api/auth/**";

    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ERROR = "/error";
}
