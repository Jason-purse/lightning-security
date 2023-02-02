package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 13:52
 * @Description token 无效处理
 */
final class AuthAuthenticationProviderUtils {
    private AuthAuthenticationProviderUtils() {
    }


    static <T extends LightningToken> DefaultLightningAuthorization invalidate(DefaultLightningAuthorization authorization, T token) {

        Assert.notNull(token.getTokenClass(),"token class must not be null !!!");

        DefaultLightningAuthorization.Builder authorizationBuilder = DefaultLightningAuthorization
                .from(authorization)
                .token(token, token.getTokenClass(),(metadata) -> {
                    metadata.put(DefaultLightningAuthorization.Token.INVALIDATED_METADATA_NAME, true);
                });

        // 如果是刷新token,访问 token 也将无效
        if (LightningToken.LightningRefreshToken.class.isAssignableFrom(token.getClass())) {
            authorizationBuilder.token(authorization.getAccessToken().getToken(), LightningToken.LightningAccessToken.class,(metadata) -> {
                metadata.put(DefaultLightningAuthorization.Token.INVALIDATED_METADATA_NAME, true);
            });
        }

        return authorizationBuilder.build();
    }
}