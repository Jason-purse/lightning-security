package com.generatera.resource.server.specification.token.jwt.config;


import com.generatera.resource.server.specification.token.jwt.config.exception.JwtException;

public class JwtEncodingException extends JwtException {
    public JwtEncodingException(String message) {
        super(message);
    }

    public JwtEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
