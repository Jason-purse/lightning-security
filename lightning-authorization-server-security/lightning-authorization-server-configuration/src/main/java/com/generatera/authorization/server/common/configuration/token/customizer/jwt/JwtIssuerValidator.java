package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningTokenValidator;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidatorResult;
import org.springframework.util.Assert;

import java.util.function.Predicate;

public final class JwtIssuerValidator implements LightningTokenValidator<LightningJwt> {
    private final JwtClaimValidator<Object> validator;

    public JwtIssuerValidator(String issuer) {
        Assert.notNull(issuer, "issuer cannot be null");
        Predicate<Object> testClaimValue = (claimValue) -> {
            return claimValue != null && issuer.equals(claimValue.toString());
        };
        this.validator = new JwtClaimValidator("iss", testClaimValue);
    }

    public LightningTokenValidatorResult validate(LightningJwt token) {
        Assert.notNull(token, "token cannot be null");
        return this.validator.validate(token);
    }
}