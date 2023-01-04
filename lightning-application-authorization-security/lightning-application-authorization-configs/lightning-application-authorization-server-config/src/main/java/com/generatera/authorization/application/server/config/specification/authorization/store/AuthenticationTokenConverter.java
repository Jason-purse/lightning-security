package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationToken;
import com.generatera.authorization.server.common.configuration.token.LightningToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;

public class AuthenticationTokenConverter implements Converter<LightningAuthenticationTokenEntity, LightningAuthenticationToken> {

    @Override
    public LightningAuthenticationToken convert(@NotNull LightningAuthenticationTokenEntity source) {
        return LightningAuthenticationToken.of(
                ElvisUtil.isNotEmptySupplier(
                        source.getAccessTokenValue(), () ->
                                LightningToken.accessToken(
                                        source.getAccessTokenValue(),
                                        Instant.ofEpochMilli(source.getAccessIssuedAt()),
                                        Instant.ofEpochMilli(source.getAccessExpiredAt())
                                )
                ),
                ElvisUtil.isNotEmptySupplier(
                        source.getRefreshTokenValue(), () ->
                                LightningToken.refreshToken(
                                        source.getRefreshTokenValue(),
                                        Instant.ofEpochMilli(source.getRefreshIssuedAt()),
                                        Instant.ofEpochMilli(source.getRefreshExpiredAt())
                                )
                )
        );
    }
}
