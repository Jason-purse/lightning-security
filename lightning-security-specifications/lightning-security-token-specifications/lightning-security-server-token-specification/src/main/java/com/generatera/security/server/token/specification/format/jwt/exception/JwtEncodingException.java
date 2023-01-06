package com.generatera.security.server.token.specification.format.jwt.exception;


public class JwtEncodingException extends JwtException {
    public JwtEncodingException(String message) {
        super(message);
    }

    public JwtEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
