package com.generatera.resource.server.specification.token.jwt.config;

import com.generatera.resource.server.specification.token.jwt.config.validator.JwtIssuerValidator;
import com.generatera.resource.server.specification.token.jwt.config.validator.JwtTimestampValidator;

import java.util.ArrayList;
import java.util.List;

public final class JwtValidators {
    private JwtValidators() {
    }

    public static LightningTokenValidator<LightningJwt> createDefaultWithIssuer(String issuer) {
        List<LightningTokenValidator<LightningJwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new JwtIssuerValidator(issuer));
        return new DelegatingLightningTokenValidator<>(validators);
    }

    public static LightningTokenValidator<LightningJwt> createDefault() {
        return new DelegatingLightningTokenValidator<LightningJwt>(List.of(new JwtTimestampValidator()));
    }
}
