package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningAuthError;
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