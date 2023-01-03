package com.generatera.authorization.server.common.configuration.token.customizer;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwt;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtDecoder;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.exception.JwtException;

public class DelegateLightningJwtDecoder implements LightningJwtDecoder  {

    private final LightningJwtDecoder delegate;
    public DelegateLightningJwtDecoder(LightningJwtDecoder delegate) {
        this.delegate = delegate;
    }
    @Override
    public LightningJwt decode(String token) throws JwtException {
        return delegate.decode(token);
    }
}
