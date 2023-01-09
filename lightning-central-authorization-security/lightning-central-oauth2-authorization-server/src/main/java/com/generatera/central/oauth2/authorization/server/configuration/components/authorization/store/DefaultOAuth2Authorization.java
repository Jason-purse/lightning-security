package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.util.Map;

public class DefaultOAuth2Authorization extends OAuth2Authorization implements LightningAuthorization {
    private final OAuth2Authorization authorization;
    public DefaultOAuth2Authorization(OAuth2Authorization oAuth2Authorization) {
        this.authorization = oAuth2Authorization;
    }
    @Override
    public String getId() {
        return authorization.getId();
    }

    @Override
    public String getRegisteredClientId() {
        return authorization.getRegisteredClientId();
    }

    @Override
    public String getPrincipalName() {
        return authorization.getPrincipalName();
    }

    @Override
    public AuthorizationGrantType getAuthorizationGrantType() {
        return authorization.getAuthorizationGrantType();
    }

    @Override
    public Token<OAuth2AccessToken> getAccessToken() {
        return authorization.getAccessToken();
    }

    @Override
    public Token<OAuth2RefreshToken> getRefreshToken() {
        return authorization.getRefreshToken();
    }

    @Override
    public <T extends OAuth2Token> Token<T> getToken(Class<T> tokenType) {
        return authorization.getToken(tokenType);
    }

    @Override
    public <T extends OAuth2Token> Token<T> getToken(String tokenValue) {
        return authorization.getToken(tokenValue);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return authorization.getAttributes();
    }

    @Override
    public <T> T getAttribute(String name) {
        return authorization.getAttribute(name);
    }

    @Override
    public boolean equals(Object obj) {
        return authorization.equals(obj);
    }

    @Override
    public int hashCode() {
        return authorization.hashCode();
    }

    @Override
    public String toString() {
        return authorization.toString();
    }
}
