package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.OAuth2AccessTokenEntity;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Optional;

/**
 *  没有token metadata 的转换器 ..
 */
public class AccessTokenEntityForTokenConverter implements Converter<OAuth2AccessToken, OAuth2AccessTokenEntity> {
    @Override
    public OAuth2AccessTokenEntity convert(@NonNull OAuth2AccessToken source) {
        return OAuth2AccessTokenEntity
                .builder()
                .accessTokenType(source.getTokenType().getValue())
                .accessTokenValue(source.getTokenValue())
                .accessTokenExpiresAt(source.getExpiresAt())
                .accessTokenIssuedAt(source.getIssuedAt())
                .accessTokenScopes(
                        Optional.ofNullable(source.getScopes())
                                .filter(ObjectUtils::isNotEmpty)
                                .map(ele -> StringUtils.joinWith(",", ele.toArray()))
                                .orElse("")
                )
                .accessTokenMetadata("")
                .build();
    }
}
