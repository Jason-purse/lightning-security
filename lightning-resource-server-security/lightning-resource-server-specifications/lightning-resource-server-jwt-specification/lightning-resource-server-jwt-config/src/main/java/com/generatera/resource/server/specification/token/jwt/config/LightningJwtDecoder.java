package com.generatera.resource.server.specification.token.jwt.config;

public interface LightningJwtDecoder {
    LightningJwt decode(String token);
}
