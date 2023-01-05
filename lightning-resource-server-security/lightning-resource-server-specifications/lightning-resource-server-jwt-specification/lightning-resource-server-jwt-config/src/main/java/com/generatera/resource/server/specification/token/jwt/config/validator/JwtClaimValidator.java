package com.generatera.resource.server.specification.token.jwt.config.validator;

import com.generatera.resource.server.config.token.LightningAuthError;
import com.generatera.resource.server.specification.token.jwt.config.LightningJwt;
import com.generatera.resource.server.specification.token.jwt.config.LightningTokenValidator;
import com.generatera.resource.server.specification.token.jwt.config.LightningTokenValidatorResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.util.function.Predicate;

public final class JwtClaimValidator<T> implements LightningTokenValidator<LightningJwt> {
    private final Log logger = LogFactory.getLog(this.getClass());
    private final String claim;
    private final Predicate<T> test;
    private final LightningAuthError error;

    public JwtClaimValidator(String claim, Predicate<T> test) {
        Assert.notNull(claim, "claim can not be null");
        Assert.notNull(test, "test can not be null");
        this.claim = claim;
        this.test = test;
        this.error = new LightningAuthError("invalid_token", "The " + this.claim + " claim is not valid", "https://tools.ietf.org/html/rfc6750#section-3.1");
    }

    public LightningTokenValidatorResult validate(LightningJwt token) {
        Assert.notNull(token, "token cannot be null");
        T claimValue = token.getClaim(this.claim);
        if (this.test.test(claimValue)) {
            return LightningTokenValidatorResult.success();
        } else {
            this.logger.debug(this.error.getDescription());
            return LightningTokenValidatorResult.failure(this.error);
        }
    }
}