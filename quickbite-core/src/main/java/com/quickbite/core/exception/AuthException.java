package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public abstract class AuthException extends BaseBusinessException {

    protected AuthException(String code, String message) {
        super(new ApiError(code, message));
    }
}
