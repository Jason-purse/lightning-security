package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;

public class DelegateLightningAuthenticationTokenService implements LightningAuthenticationTokenService {

    private final LightningAuthenticationTokenService tokenService;

    public DelegateLightningAuthenticationTokenService(LightningAuthenticationTokenService tokenService) {
        this.tokenService = tokenService;
    }
    @Override
    public void save(DefaultLightningAuthorization authorization) {
        this.tokenService.save(authorization);
    }

    @Override
    public void remove(DefaultLightningAuthorization authorization) {
        this.tokenService.remove(authorization);
    }

    @Override
    public DefaultLightningAuthorization findAuthorizationById(String id) {
        return this.tokenService.findAuthorizationById(id);
    }

    @Override
    public DefaultLightningAuthorization findByToken(String token, LightningTokenType.LightningAuthenticationTokenType tokenType) {
        return this.tokenService.findByToken(token,tokenType);
    }
}
