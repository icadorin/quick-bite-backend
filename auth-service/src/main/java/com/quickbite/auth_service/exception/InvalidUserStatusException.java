package com.quickbite.auth_service.exception;

public class InvalidUserStatusException extends AuthException {
    public InvalidUserStatusException(String message) {
        super(message);
    }
}
