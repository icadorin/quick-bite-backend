package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public class InvalidTokenException extends BaseBusinessException {
    public InvalidTokenException(String message) {
        super(new ApiError("JWT_VALIDATION_ERROR", message));
    }
}
