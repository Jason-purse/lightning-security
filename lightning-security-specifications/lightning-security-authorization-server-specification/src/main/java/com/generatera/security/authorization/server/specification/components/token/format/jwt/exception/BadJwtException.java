package com.generatera.security.authorization.server.specification.components.token.format.jwt.exception;

public class BadJwtException extends JwtException {
    public BadJwtException(String message) {
        super(message);
    }

    public BadJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}