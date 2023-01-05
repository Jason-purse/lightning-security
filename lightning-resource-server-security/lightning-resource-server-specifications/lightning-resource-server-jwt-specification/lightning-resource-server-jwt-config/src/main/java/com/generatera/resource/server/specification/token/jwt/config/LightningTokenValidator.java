package com.generatera.resource.server.specification.token.jwt.config;

import com.generatera.resource.server.config.token.LightningToken;

@FunctionalInterface
public interface LightningTokenValidator<T extends LightningToken> {
    LightningTokenValidatorResult validate(T token);
}