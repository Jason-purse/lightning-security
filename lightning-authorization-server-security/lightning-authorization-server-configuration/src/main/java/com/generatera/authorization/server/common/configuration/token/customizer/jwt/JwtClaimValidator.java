package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningAuthError;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidator;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidatorResult;
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
            return LightningTokenValidatorResult.failure(new LightningAuthError[]{this.error});
        }
    }
}