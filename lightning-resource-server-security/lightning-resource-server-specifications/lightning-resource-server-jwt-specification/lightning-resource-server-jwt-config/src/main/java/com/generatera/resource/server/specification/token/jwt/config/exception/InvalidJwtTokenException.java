package com.generatera.resource.server.specification.token.jwt.config.exception;

import com.generatera.resource.server.config.token.LightningAuthError;
import com.generatera.resource.server.config.token.LightningAuthenticationException;

public class InvalidJwtTokenException extends LightningAuthenticationException {
    public InvalidJwtTokenException(String description) {
        super(new LightningAuthError(""));
    }

    public InvalidJwtTokenException(String description, Throwable cause) {
        super(new LightningAuthError(""), cause);
    }
}
