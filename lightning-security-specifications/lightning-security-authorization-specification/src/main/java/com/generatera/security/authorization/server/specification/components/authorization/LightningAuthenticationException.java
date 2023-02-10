package com.generatera.security.authorization.server.specification.components.authorization;

import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public class LightningAuthenticationException extends AuthenticationException {
    private final LightningAuthError error;

    public LightningAuthenticationException(String errorCode) {
        this(new LightningAuthError(errorCode));
    }

    public LightningAuthenticationException(LightningAuthError error) {
        this(error, error.getDescription());
    }

    public LightningAuthenticationException(LightningAuthError error, Throwable cause) {
        this(error, cause.getMessage(), cause);
    }

    public LightningAuthenticationException(LightningAuthError error, String message) {
        this(error, message, (Throwable)null);
    }

    public LightningAuthenticationException(LightningAuthError error, String message, Throwable cause) {
        super(message, cause);
        Assert.notNull(error, "error cannot be null");
        this.error = error;
    }

    public LightningAuthError getError() {
        return this.error;
    }
}