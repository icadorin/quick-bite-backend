package com.quickbite.auth_service.exception;

public class ValidationException extends AuthException {
    public ValidationException(String message) {
        super(message);
    }
}
