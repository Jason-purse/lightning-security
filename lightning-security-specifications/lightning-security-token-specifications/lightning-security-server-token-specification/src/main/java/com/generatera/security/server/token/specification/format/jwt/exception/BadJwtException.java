package com.generatera.security.server.token.specification.format.jwt.exception;

public class BadJwtException extends JwtException {
    public BadJwtException(String message) {
        super(message);
    }

    public BadJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}