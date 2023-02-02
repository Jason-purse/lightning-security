package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuth2RefreshTokenEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
/**
 *  没有token metadata 的转换器 ..
 */
public class RefreshTokenEntityForTokenConverter implements Converter<OAuth2RefreshToken, OAuth2RefreshTokenEntity> {
    @Override
    public OAuth2RefreshTokenEntity convert(@NonNull OAuth2RefreshToken source) {
        return OAuth2RefreshTokenEntity
                .builder()
                .refreshTokenValue(source.getTokenValue())
                .refreshTokenIssuedAt(source.getIssuedAt())
                .refreshTokenExpiresAt(source.getExpiresAt())
                .refreshTokenMetadata("")
                .build();
    }
}
