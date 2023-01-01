package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuth2RefreshTokenEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

public class RefreshTokenConverter implements Converter<OAuth2RefreshTokenEntity, OAuth2RefreshToken> {
    @Override
    public OAuth2RefreshToken convert(@NonNull OAuth2RefreshTokenEntity source) {
        return new OAuth2RefreshToken(
                source.getRefreshTokenValue(),
                source.getRefreshTokenIssuedAt(),
                source.getRefreshTokenExpiresAt()
        );
    }
}
