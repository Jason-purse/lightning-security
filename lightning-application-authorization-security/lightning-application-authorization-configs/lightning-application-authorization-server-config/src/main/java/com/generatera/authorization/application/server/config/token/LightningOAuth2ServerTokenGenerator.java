package com.generatera.authorization.application.server.config.token;

import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * oauth2 server token generator 扩展点
 */
public interface LightningOAuth2ServerTokenGenerator extends OAuth2TokenGenerator<OAuth2Token> {
}
