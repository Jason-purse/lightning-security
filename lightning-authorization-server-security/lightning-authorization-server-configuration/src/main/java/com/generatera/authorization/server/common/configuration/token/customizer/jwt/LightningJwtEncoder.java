package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import org.springframework.security.oauth2.jwt.JwtEncodingException;

/**
 * oauth2 jwtEncoder copy (隔离oauth2 依赖)
 */
@FunctionalInterface
public interface LightningJwtEncoder {
    LightningJwt encode(JwtEncoderParameters parameters) throws JwtEncodingException;
}