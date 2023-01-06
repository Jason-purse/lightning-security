package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.model.entity.LightningAuthenticationTokenEntity;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.core.convert.converter.Converter;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 14:10
 * @Description Authentication Token Entity converter
 */
public class AuthenticationTokenEntityConverter implements Converter<LightningApplicationLevelAuthenticationToken, LightningAuthenticationTokenEntity> {
    @Override
    public LightningAuthenticationTokenEntity convert(LightningApplicationLevelAuthenticationToken source) {

        LightningAuthenticationTokenEntity.LightningAuthenticationTokenEntityBuilder builder = LightningAuthenticationTokenEntity
                .builder();
        ElvisUtil.isNotEmptyConsumer(source.accessToken(),token -> {
            builder.accessTokenValue(token.getTokenValue());
            builder.accessExpiredAt(token.getExpiresAt().toEpochMilli());
            builder.accessIssuedAt(token.getIssuedAt().toEpochMilli());

            //builder.accessTokenType(token.getTokenType().getTokenTypeString());
        });

        ElvisUtil.isNotEmptyConsumer(source.refreshToken(),token -> {
            builder.refreshTokenValue(token.getTokenValue());
            builder.refreshIssuedAt(token.getIssuedAt().toEpochMilli());
            builder.refreshExpiredAt(token.getExpiresAt().toEpochMilli());
            //builder.refreshTokenType(token.getTokenType().getTokenTypeString());
        });

        return builder.build();
    }
}
