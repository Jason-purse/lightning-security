package com.generatera.authorization.server.common.configuration.token.customizer.jwt;


import com.generatera.authorization.server.common.configuration.token.customizer.jwt.exception.JwtException;

public class JwtEncodingException extends JwtException {
    public JwtEncodingException(String message) {
        super(message);
    }

    public JwtEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
