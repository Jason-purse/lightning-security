package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.authorization.server.common.configuration.model.entity.DefaultAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator.LightningAuthenticationAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningRefreshTokenGenerator.LightningAuthenticationRefreshToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;

/**
 * 建议继承{@link OptimizedAuthenticationTokenConverter} 而不是当前类 ...
 * 并且如果存在不同的{@link LightningAuthenticationTokenEntity}可以根据当前类的
 * {@link #getUserPrincipal(LightningAuthenticationTokenEntity)} 进行额外的
 * {@link LightningAuthenticationTokenEntity} 判断并获取 userPrincipal ...
 */
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
                                                new LightningTokenType.LightningTokenValueType(source.getAccessTokenType()),
                                                // todo 应该是 token Value format 而不是 token value type format
                                                new LightningTokenType.LightningTokenValueFormat(source.getAccessTokenTypeFormat())
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
                )
                .attribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME,getUserPrincipal(source))
                .build();
    }



    protected Object getUserPrincipal(LightningAuthenticationTokenEntity entity) {
        if (entity instanceof DefaultAuthenticationTokenEntity defaultTokenEntity) {
            return defaultTokenEntity.getUserPrincipal();
        }
        else if(entity instanceof ForDBAuthenticationTokenEntity tokenEntity) {
            return tokenEntity.getUserPrincipal();
        }
        throw new IllegalArgumentException("can't acquire user principal !!!");
    }
}
