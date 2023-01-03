package com.generatera.authorization.server.common.configuration.token;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public final class DelegatingLightningTokenValidator<T extends LightningToken> implements LightningTokenValidator<T> {
    private final Collection<LightningTokenValidator<T>> tokenValidators;

    public DelegatingLightningTokenValidator(Collection<LightningTokenValidator<T>> tokenValidators) {
        Assert.notNull(tokenValidators, "tokenValidators cannot be null");
        this.tokenValidators = new ArrayList(tokenValidators);
    }

    @SafeVarargs
    public DelegatingLightningTokenValidator(LightningTokenValidator<T>... tokenValidators) {
        this((Collection) Arrays.asList(tokenValidators));
    }

    public LightningTokenValidatorResult validate(T token) {
        Collection<LightningAuthError> errors = new ArrayList();
        Iterator var3 = this.tokenValidators.iterator();

        while(var3.hasNext()) {
            LightningTokenValidator<T> validator = (LightningTokenValidator<T>)var3.next();
            errors.addAll(validator.validate(token).getErrors());
        }

        return LightningTokenValidatorResult.failure(errors);
    }
}
