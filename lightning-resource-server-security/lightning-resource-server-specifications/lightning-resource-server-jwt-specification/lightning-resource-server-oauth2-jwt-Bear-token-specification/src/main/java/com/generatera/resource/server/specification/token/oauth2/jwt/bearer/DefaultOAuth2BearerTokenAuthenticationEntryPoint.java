package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import com.generatera.resource.server.config.token.entrypoint.DelegateAuthenticationEntryPoint;
import com.generatera.resource.server.config.token.entrypoint.LightningAuthenticationEntryPoint;

public class DefaultOAuth2BearerTokenAuthenticationEntryPoint extends DelegateAuthenticationEntryPoint implements OAuth2BearerTokenAuthenticationEntryPoint {

    public DefaultOAuth2BearerTokenAuthenticationEntryPoint(LightningAuthenticationEntryPoint entryPoint) {
        super(entryPoint);
    }
}
