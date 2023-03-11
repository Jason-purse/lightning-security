package com.generatera.authorization.application.server.oauth2.login.config.token.response;

import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.Assert;

public class DelegateOAuth2AccessTokenResponseClient<T extends AbstractOAuth2AuthorizationGrantRequest> implements LightningOAuth2AccessTokenResponseClient<T> {
    private final  LightningOAuth2AccessTokenResponseClient<T> responseClient;
    public DelegateOAuth2AccessTokenResponseClient(
            LightningOAuth2AccessTokenResponseClient<T> responseClient
    ) {
        Assert.notNull(responseClient,"responseClient must not be null");
        this.responseClient = responseClient;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(T authorizationGrantRequest) {
        return responseClient.getTokenResponse(authorizationGrantRequest);
    }
}
