package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.exception.JwtEncodingException;

/**
 * oauth2 jwtEncoder copy (隔离oauth2 依赖)
 * Lightning Jwt 编码器 ..
 */
@FunctionalInterface
public interface LightningJwtEncoder {
    LightningJwt encode(JwtEncoderParameters parameters) throws JwtEncodingException;
}