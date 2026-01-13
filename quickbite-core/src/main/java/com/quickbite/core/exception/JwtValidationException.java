package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public class JwtValidationException extends BaseBusinessException {

    public JwtValidationException(String message) {
        super(new ApiError("JWT_VALIDATION_ERROR", message));
    }
}
