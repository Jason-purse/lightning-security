package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;

public class OAuth2ClientSecretAuthenticationToken extends AbstractAuthenticationToken {

    private final String clientId;

    private final String clientSecret;

    @Nullable
    private Authentication userPrincipal;

    public OAuth2ClientSecretAuthenticationToken(String clientId,String clientSecret) {
      this(clientId,clientSecret,null);
    }
    public OAuth2ClientSecretAuthenticationToken(String clientId,String clientSecret,Authentication userPrincipal) {
        super(Collections.emptyList());
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userPrincipal = userPrincipal;
        if(userPrincipal != null) {
            setAuthenticated(true);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }


    @Override
    public Object getCredentials() {
        return clientSecret;
    }

    @Override
    public Object getPrincipal() {
        return userPrincipal;
    }
}
