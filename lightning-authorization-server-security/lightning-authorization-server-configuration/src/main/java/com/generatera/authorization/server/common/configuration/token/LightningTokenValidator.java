package com.generatera.authorization.server.common.configuration.token;

@FunctionalInterface
public interface LightningTokenValidator<T extends LightningToken> {
    LightningTokenValidatorResult validate(T token);
}