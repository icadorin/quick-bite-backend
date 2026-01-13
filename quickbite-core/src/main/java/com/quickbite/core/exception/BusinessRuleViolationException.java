package com.quickbite.core.exception;

import com.quickbite.core.api.ApiError;

public class BusinessRuleViolationException extends BaseBusinessException {

    public BusinessRuleViolationException(String message) {
        super(new ApiError("BUSINESS_RULE_VIOLATION", message));
    }
}
