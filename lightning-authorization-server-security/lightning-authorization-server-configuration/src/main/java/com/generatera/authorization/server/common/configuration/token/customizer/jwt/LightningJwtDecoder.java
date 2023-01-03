package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.exception.JwtException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * jwt decoder 隔离oauth2的强依赖,例如不使用oauth2 ..
 */
@FunctionalInterface
public interface LightningJwtDecoder {
    LightningJwt decode(String token) throws JwtException;

    /**
     * 默认使用 Nimbus JwtDecoder ...
     * @param jwkSource
     * @return
     */
    public static LightningJwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return new DefaultLightningJwtDecoder(jwkSource);
    }
}