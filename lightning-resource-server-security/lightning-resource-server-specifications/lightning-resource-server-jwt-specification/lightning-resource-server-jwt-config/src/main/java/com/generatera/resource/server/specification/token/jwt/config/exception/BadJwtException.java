package com.generatera.resource.server.specification.token.jwt.config.exception;

public class BadJwtException extends JwtException {
    public BadJwtException(String message) {
        super(message);
    }

    public BadJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}