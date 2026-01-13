package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public class ResourceNotFoundException extends BaseBusinessException {

    public ResourceNotFoundException(String message) {
        super(new ApiError("RESOURCE_NOT_FOUND", message));
    }
}
