package com.generatera.resource.server.specification.token.jwt.config.validator;

import com.generatera.resource.server.specification.token.jwt.config.LightningJwt;
import com.generatera.resource.server.specification.token.jwt.config.LightningTokenValidator;
import com.generatera.resource.server.specification.token.jwt.config.LightningTokenValidatorResult;
import org.springframework.util.Assert;

import java.util.function.Predicate;

public final class JwtIssuerValidator implements LightningTokenValidator<LightningJwt> {
    private final JwtClaimValidator<Object> validator;

    public JwtIssuerValidator(String issuer) {
        Assert.notNull(issuer, "issuer cannot be null");
        Predicate<Object> testClaimValue = (claimValue) -> {
            return claimValue != null && issuer.equals(claimValue.toString());
        };
        this.validator = new JwtClaimValidator<>("iss", testClaimValue);
    }

    public LightningTokenValidatorResult validate(LightningJwt token) {
        Assert.notNull(token, "token cannot be null");
        return this.validator.validate(token);
    }
}