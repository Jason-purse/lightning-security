package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import org.springframework.security.oauth2.jwt.JwtException;

public class JwtEncodingException extends JwtException {
    public JwtEncodingException(String message) {
        super(message);
    }

    public JwtEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
