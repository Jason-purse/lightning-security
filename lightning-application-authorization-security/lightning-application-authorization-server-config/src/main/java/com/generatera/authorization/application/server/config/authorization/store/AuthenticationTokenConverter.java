package com.generatera.authorization.application.server.config.authorization.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.application.server.config.model.entity.DefaultAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.ForDBAuthenticationTokenEntity;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator.LightningAuthenticationAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningRefreshTokenGenerator.LightningAuthenticationRefreshToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.util.JsonUtil;
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
        DefaultLightningAuthorization.Builder builder = new DefaultLightningAuthorization.Builder()
                .id(source.getId())
                .principalName(source.getPrincipalName());

        ElvisUtil.isNotEmptyConsumer(
                source.getAccessTokenValue(), value ->
                {
                    builder.token(new LightningAuthenticationAccessToken(
                            new DefaultPlainToken(
                                    source.getAccessTokenValue(),
                                    Instant.ofEpochMilli(source.getAccessIssuedAt()),
                                    Instant.ofEpochMilli(source.getAccessExpiredAt())
                            ),
                            new LightningTokenType.LightningTokenValueType(source.getAccessTokenType()),
                            new LightningTokenType.LightningTokenValueFormat(source.getAccessTokenValueFormat())
                    ), LightningToken.LightningAccessToken.class, metadataMap ->
                            ElvisUtil.isNotEmptyConsumer(source.getAccessTokenMetadata(), metaData ->
                            metadataMap.putAll(JsonUtil.getDefaultJsonUtil().fromJson(source.getAccessTokenMetadata(), new TypeReference<>() {
                    }))));
                }
        );

        ElvisUtil.isNotEmptyConsumer(
                source.getRefreshTokenValue(), value ->
                {
                    builder.token(new LightningAuthenticationRefreshToken(
                            new DefaultPlainToken(
                                    value,
                                    Instant.ofEpochMilli(source.getRefreshIssuedAt()),
                                    Instant.ofEpochMilli(source.getRefreshExpiredAt())
                            )
                    ), LightningToken.LightningRefreshToken.class, metaDataMap ->
                            ElvisUtil.isNotEmptyConsumer(source.getRefreshTokenMetadata(), metaData ->
                                    metaDataMap.putAll(JsonUtil.getDefaultJsonUtil().fromJson(source.getAccessTokenMetadata(), new TypeReference<>() {
                                    }))));
                }
        );

        return builder
                .attribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME, getUserPrincipal(source))
                .build();
    }


    protected Object getUserPrincipal(LightningAuthenticationTokenEntity entity) {
        if (entity instanceof DefaultAuthenticationTokenEntity defaultTokenEntity) {
            return defaultTokenEntity.getUserPrincipal();
        } else if (entity instanceof ForDBAuthenticationTokenEntity tokenEntity) {
            return tokenEntity.getUserPrincipal();
        }
        throw new IllegalArgumentException("can't acquire user principal !!!");
    }
}
