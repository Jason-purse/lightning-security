package com.generatera.authorization.server.common.configuration.token;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class LightningTokenValidatorResult {

    static final LightningTokenValidatorResult NO_ERRORS = new LightningTokenValidatorResult(Collections.emptyList());
    private final Collection<LightningAuthError> errors;

    private LightningTokenValidatorResult(Collection<LightningAuthError> errors) {
        Assert.notNull(errors, "errors cannot be null");
        this.errors = new ArrayList(errors);
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public Collection<LightningAuthError> getErrors() {
        return this.errors;
    }

    public static LightningTokenValidatorResult success() {
        return NO_ERRORS;
    }

    public static LightningTokenValidatorResult failure(LightningAuthError... errors) {
        return failure((Collection) Arrays.asList(errors));
    }

    public static LightningTokenValidatorResult failure(Collection<LightningAuthError> errors) {
        return errors.isEmpty() ? NO_ERRORS : new LightningTokenValidatorResult(errors);
    }
}
