package com.quickbite.core.exception;

public class TokenException extends AuthException {

    public TokenException(String message) {
        super("TOKEN_ERROR", message);
    }
}
