package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator.LightningAuthenticationAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningRefreshTokenGenerator.LightningAuthenticationRefreshToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
// TODO
public class AuthenticationTokenConverter implements Converter<LightningAuthenticationTokenEntity, DefaultLightningAuthorization> {

    @Override
    public DefaultLightningAuthorization convert(@NotNull LightningAuthenticationTokenEntity source) {
        return new DefaultLightningAuthorization.Builder()
                .accessToken(
                        ElvisUtil.isNotEmptySupplier(
                                source.getAccessTokenValue(), () ->
                                        new LightningAuthenticationAccessToken(
                                                new DefaultPlainToken(
                                                        source.getAccessTokenValue(),
                                                        Instant.ofEpochMilli(source.getAccessIssuedAt()),
                                                        Instant.ofEpochMilli(source.getAccessExpiredAt())
                                                ),
                                                LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE
                                        )
                        ))
                .refreshToken(
                        ElvisUtil.isNotEmptySupplier(
                                source.getRefreshTokenValue(), () ->
                                        new LightningAuthenticationRefreshToken(
                                                new DefaultPlainToken(
                                                        source.getRefreshTokenValue(),
                                                        Instant.ofEpochMilli(source.getRefreshIssuedAt()),
                                                        Instant.ofEpochMilli(source.getRefreshExpiredAt())
                                                )
                                        )
                        )
                ).build();
    }
}
