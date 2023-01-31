package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 14:10
 * @Description Authentication Token Entity converter
 *
 * 子类应该继承 {@link OptimizedAuthenticationTokenEntityConverter} 而不是当前类,因为妥善处理了
 * {@link com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization#USER_INFO_ATTRIBUTE_NAME}
 */
public class AuthenticationTokenEntityConverter implements Converter<DefaultLightningAuthorization, LightningAuthenticationTokenEntity> {
    @Override
    public LightningAuthenticationTokenEntity convert(DefaultLightningAuthorization source) {

        LightningAuthenticationTokenEntity.LightningAuthenticationTokenEntityBuilder builder = LightningAuthenticationTokenEntity
                .builder();
        builder.id(source.getId());
        ElvisUtil.isNotEmptyConsumer(source.getAccessToken(),token -> {
            LightningToken.LightningAccessToken accessToken = token.getToken();
            builder.accessTokenValue(accessToken.getTokenValue());
            assert accessToken.getExpiresAt() != null;
            assert accessToken.getIssuedAt() != null;
            builder.accessExpiredAt(accessToken.getExpiresAt().toEpochMilli());
            builder.accessIssuedAt(accessToken.getIssuedAt().toEpochMilli());
            builder.accessTokenType(accessToken.getTokenValueType().value());
            builder.accessTokenValueFormat(accessToken.getTokenValueFormat().value());
            builder.accessTokenMetadata(JsonUtil.getDefaultJsonUtil().asJSON(token.getMetadata()));
        });

        ElvisUtil.isNotEmptyConsumer(source.getRefreshToken(),token -> {
            LightningToken.LightningRefreshToken refreshToken = token.getToken();
            builder.refreshTokenValue(refreshToken.getTokenValue());
            assert refreshToken.getExpiresAt() != null;
            assert refreshToken.getIssuedAt() != null;
            builder.refreshIssuedAt(refreshToken.getIssuedAt().toEpochMilli());
            builder.refreshExpiredAt(refreshToken.getExpiresAt().toEpochMilli());
            builder.refreshTokenMetadata(JsonUtil.getDefaultJsonUtil().asJSON(token.getMetadata()));
        });
        builder.principalName(source.getPrincipalName());

        // 子类 需要处理 userPrincipal..
        return builder.build();
    }
}
