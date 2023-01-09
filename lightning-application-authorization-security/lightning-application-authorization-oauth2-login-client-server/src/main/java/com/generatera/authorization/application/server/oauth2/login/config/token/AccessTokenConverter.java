package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuth2AccessTokenEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class AccessTokenConverter implements Converter<OAuth2AccessTokenEntity, OAuth2AccessToken> {
    @Override
    public OAuth2AccessToken convert(@NonNull OAuth2AccessTokenEntity source) {
        return new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                source.getAccessTokenValue(),
                source.getAccessTokenIssuedAt(),
                source.getAccessTokenExpiresAt());
    }
}
