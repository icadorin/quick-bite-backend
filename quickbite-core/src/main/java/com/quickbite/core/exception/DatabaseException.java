package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public class DatabaseException extends BaseBusinessException {

    public DatabaseException(String message) {
        super(new ApiError("DATABASE_ERROR", message));
    }

    public DatabaseException(String message, Throwable cause) {
        super(new ApiError("DATABASE_ERROR", message));
        initCause(cause);
    }
}
