package com.generatera.resource.server.specification.token.jwt.config.exception;

import com.generatera.resource.server.config.token.LightningAuthError;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;

public class JwtValidationException extends BadJwtException {
    private final Collection<LightningAuthError> errors;

    public JwtValidationException(String message, Collection<LightningAuthError> errors) {
        super(message);
        Assert.notEmpty(errors, "errors cannot be empty");
        this.errors = new ArrayList<>(errors);
    }

    public Collection<LightningAuthError> getErrors() {
        return this.errors;
    }
}