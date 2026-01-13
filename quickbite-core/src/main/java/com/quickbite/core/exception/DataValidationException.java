package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public class DataValidationException extends BaseBusinessException {

    public DataValidationException(String message) {
        super(new ApiError("DATA_VALIDATION_ERROR", message));
    }
}
