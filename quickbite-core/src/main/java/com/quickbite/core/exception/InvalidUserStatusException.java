package com.quickbite.core.exception;

public class InvalidUserStatusException extends AuthException {

    public InvalidUserStatusException(String message) {
        super("INVALID_USER_STATUS", message);
    }
}
