package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.service;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.DefaultOAuth2Authorization;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 15:44
 * @Description 默认基于内存的OAuth2 Authorization Service ..
 */
public class DefaultOAuth2AuthorizationService implements OAuth2AuthorizationService, LightningAuthorizationService<DefaultOAuth2Authorization> {

    private final InMemoryOAuth2AuthorizationService delegate = new InMemoryOAuth2AuthorizationService();

    @Override
    public void save(DefaultOAuth2Authorization authorization) {
        this.delegate.save(authorization);
    }

    @Override
    public void remove(DefaultOAuth2Authorization authorization) {
        this.delegate.remove(authorization);
    }

    @Override
    public DefaultOAuth2Authorization findAuthorizationById(String id) {
        OAuth2Authorization authorization = this.delegate.findById(id);
        return authorization != null ? new DefaultOAuth2Authorization(authorization) : null;
    }

    @Override
    public DefaultOAuth2Authorization findByToken(String token, LightningTokenType.LightningAuthenticationTokenType tokenType) {
        OAuth2Authorization authorization = findByToken(token, tokenType != null ? new OAuth2TokenType(tokenType.value()) : null);
        return authorization != null ? new DefaultOAuth2Authorization(
                authorization
        ) : null;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        this.save(new DefaultOAuth2Authorization(authorization));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        this.delegate.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return this.findAuthorizationById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return this.delegate.findByToken(token, tokenType);
    }
}
