package com.generatera.authorization.application.server.oauth2.login.config.token.response;

import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

/**
 * 扩展为 Lightning  access Token Response Client
 */
public interface LightningOAuth2AccessTokenResponseClient extends OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
}
