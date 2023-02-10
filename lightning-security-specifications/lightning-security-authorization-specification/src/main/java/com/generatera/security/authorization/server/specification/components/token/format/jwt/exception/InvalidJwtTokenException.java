package com.generatera.security.authorization.server.specification.components.token.format.jwt.exception;

import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;

public class InvalidJwtTokenException extends LightningAuthenticationException {
    public InvalidJwtTokenException(String description) {
        super(new LightningAuthError(""));
    }

    public InvalidJwtTokenException(String description, Throwable cause) {
        super(new LightningAuthError(""), cause);
    }
}
