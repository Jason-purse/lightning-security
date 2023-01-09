package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.util.Map;

public class DefaultOauth2LightningAuthorization extends DefaultLightningAuthorization {

    private String registeredClientId;
    private AuthorizationGrantType authorizationGrantType;
    private Map<Class<? extends OAuth2Token>, OAuth2Authorization.Token<?>> tokens;
    private Map<String, Object> attributes;
}
