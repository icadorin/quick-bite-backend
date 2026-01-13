package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;
import lombok.Getter;

@Getter
public class BaseBusinessException extends RuntimeException {

    private final ApiError apiError;

    protected BaseBusinessException(ApiError apiError) {
        super(apiError.message());
        this.apiError = apiError;
    }
}
