package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.DelegatingLightningTokenValidator;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidator;

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
