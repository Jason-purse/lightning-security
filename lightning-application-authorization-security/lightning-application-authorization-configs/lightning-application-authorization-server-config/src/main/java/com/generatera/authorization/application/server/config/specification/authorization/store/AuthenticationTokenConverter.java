package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
import com.generatera.security.server.token.specification.LightningToken;
import com.generatera.security.server.token.specification.LightningTokenType;
import com.generatera.security.server.token.specification.type.LightningAccessToken;
import com.generatera.security.server.token.specification.type.LightningRefreshToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;

public class AuthenticationTokenConverter implements Converter<LightningAuthenticationTokenEntity, LightningApplicationLevelAuthenticationToken> {

    @Override
    public LightningApplicationLevelAuthenticationToken convert(@NotNull LightningAuthenticationTokenEntity source) {
        return LightningApplicationLevelAuthenticationToken.of(
                ElvisUtil.isNotEmptySupplier(
                        source.getAccessTokenValue(), () ->
                                new LightningAccessToken(
                                        new LightningToken.ComplexToken(
                                                LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                                                source.getAccessTokenValue(),
                                                Instant.ofEpochMilli(source.getAccessIssuedAt()),
                                                Instant.ofEpochMilli(source.getAccessExpiredAt())
                                        ) {
                                        }
                                )
                ),
                ElvisUtil.isNotEmptySupplier(
                        source.getRefreshTokenValue(), () ->
                                new LightningRefreshToken(
                                        new LightningToken.ComplexToken(
                                                LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                                                source.getRefreshTokenValue(),
                                                Instant.ofEpochMilli(source.getRefreshIssuedAt()),
                                                Instant.ofEpochMilli(source.getRefreshExpiredAt())
                                        ) {
                                        }
                                )
                )
        );
    }
}
