package com.generatera.security.server.token.specification.format.jwt.exception;

import com.generatera.security.authorization.server.specification.endpoints.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.endpoints.authorization.LightningAuthenticationException;

public class InvalidJwtTokenException extends LightningAuthenticationException {
    public InvalidJwtTokenException(String description) {
        super(new LightningAuthError(""));
    }

    public InvalidJwtTokenException(String description, Throwable cause) {
        super(new LightningAuthError(""), cause);
    }
}
