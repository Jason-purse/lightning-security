package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
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
        DefaultLightningAuthorization.Builder authorizationBuilder = DefaultLightningAuthorization
                .from(authorization)
                .token(token, (metadata) -> {
                    metadata.put(DefaultLightningAuthorization.Token.INVALIDATED_METADATA_NAME, true);
                });
        if (LightningToken.LightningRefreshToken.class.isAssignableFrom(token.getClass())) {
            authorizationBuilder.token(authorization.getAccessToken().getToken(), (metadata) -> {
                metadata.put(DefaultLightningAuthorization.Token.INVALIDATED_METADATA_NAME, true);
            });
        }

        return authorizationBuilder.build();
    }
}