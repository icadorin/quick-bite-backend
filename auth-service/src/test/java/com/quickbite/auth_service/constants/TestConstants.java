package com.quickbite.auth_service.constants;

import com.quickbite.auth_service.dto.UserResponse;

public class TestConstants {

    public static final Long VALID_USER_ID = 1L;

    public static final String VALID_EMAIL = "user@test.com";
    public static final String INVALID_EMAIL = "invalid-email";
    public static final String NON_EXISTENT_EMAIL = "nonexistent@test.com";

    public static final String VALID_PASSWORD = "Password123";
    public static final String SHORT_PASSWORD = "Invalid credentials";

    public static final String VALID_FULL_NAME = "John John";

    public static final String VALID_REFRESH_TOKEN = "valid-refresh-token";
    public static final String INVALID_REFRESH_TOKEN = "";

    public static final String VALID_ACCESS_TOKEN = "valid-access-token";

    public static final Long TOKEN_EXPIRATION_SECONDS = 3600L;

    public static final UserResponse USER_RESPONSE =
        UserResponse.builder()
            .id(VALID_USER_ID)
            .email(VALID_EMAIL)
            .fullName(VALID_FULL_NAME)
            .role("CUSTOMER")
            .build();

    public static final String EMAIL_REQUIRED_MESSAGE =
        "Email is required";

    public static final String EMAIL_INVALID_MESSAGE =
        "Email must be valid";

    public static final String PASSWORD_REQUIRED_MESSAGE =
        "Password is required";

    public static final String PASSWORD_TOO_SHORT_MESSAGE =
        "Password must be at least 8 characters long";

    public static final String NAME_REQUIRED_MESSAGE =
        "Name is required";

    public static final String REFRESH_TOKEN_REQUIRED_MESSAGE =
        "Refresh token is required";
}
