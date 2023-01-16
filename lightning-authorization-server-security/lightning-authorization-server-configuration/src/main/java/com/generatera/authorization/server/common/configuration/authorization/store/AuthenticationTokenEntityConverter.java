package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.core.convert.converter.Converter;

import static com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization.USER_INFO_ATTRIBUTE_NAME;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 14:10
 * @Description Authentication Token Entity converter
 */
public class AuthenticationTokenEntityConverter implements Converter<DefaultLightningAuthorization, LightningAuthenticationTokenEntity> {
    @Override
    public LightningAuthenticationTokenEntity convert(DefaultLightningAuthorization source) {

        LightningAuthenticationTokenEntity.LightningAuthenticationTokenEntityBuilder builder = LightningAuthenticationTokenEntity
                .builder();
        ElvisUtil.isNotEmptyConsumer(source.getAccessToken(),token -> {
            LightningToken.LightningAccessToken accessToken = token.getToken();
            builder.accessTokenValue(accessToken.getTokenValue());
            assert accessToken.getExpiresAt() != null;
            assert accessToken.getIssuedAt() != null;
            builder.accessExpiredAt(accessToken.getExpiresAt().toEpochMilli());
            builder.accessIssuedAt(accessToken.getIssuedAt().toEpochMilli());
        });

        ElvisUtil.isNotEmptyConsumer(source.getRefreshToken(),token -> {
            LightningToken.LightningRefreshToken refreshToken = token.getToken();
            builder.refreshTokenValue(refreshToken.getTokenValue());
            assert refreshToken.getExpiresAt() != null;
            assert refreshToken.getIssuedAt() != null;
            builder.refreshIssuedAt(refreshToken.getIssuedAt().toEpochMilli());
            builder.refreshExpiredAt(refreshToken.getExpiresAt().toEpochMilli());
        });

        Object userPrincipal = source.getAttribute(USER_INFO_ATTRIBUTE_NAME);
        builder.userPrincipal(userPrincipal);

        return builder.build();
    }
}
