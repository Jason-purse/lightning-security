package com.generatera.resource.server.specification.token.jwt.config;

/**
 * oauth2 jwtEncoder copy (隔离oauth2 依赖)
 */
@FunctionalInterface
public interface LightningJwtEncoder {
    LightningJwt encode(JwtEncoderParameters parameters) throws JwtEncodingException;
}