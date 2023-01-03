package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.exception.JwtException;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose.NimbusJwtDecoder;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

public class DefaultLightningJwtDecoder implements LightningJwtDecoder {

    private final LightningJwtDecoder delegate;
    public DefaultLightningJwtDecoder(JWKSource<SecurityContext> source) {
        this.delegate = NimbusJwtDecoder.jwtDecoder(source);
    }
    @Override
    public LightningJwt decode(String token) throws JwtException {
        return delegate.decode(token);
    }
}
