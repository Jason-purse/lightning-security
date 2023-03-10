package com.generatera.authorization.application.server.oauth2.login.config.token.response;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public class DefaultOAuth2AccessTokenResponseClient implements LightningOAuth2AccessTokenResponseClient {
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        return tokenResponseClient.getTokenResponse(authorizationGrantRequest);
    }
}
