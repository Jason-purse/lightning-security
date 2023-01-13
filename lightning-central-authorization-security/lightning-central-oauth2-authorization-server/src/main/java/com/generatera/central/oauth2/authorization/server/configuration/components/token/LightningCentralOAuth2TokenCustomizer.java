package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 12:31
 * @Description oauth2 Token 定制化器
 */
public interface LightningCentralOAuth2TokenCustomizer<T extends OAuth2TokenContext> extends OAuth2TokenCustomizer<T> {

    /**
     * oauth2 access token customizer ..
     */
    interface LightningCentralOAuth2AccessTokenCustomizer extends LightningCentralOAuth2TokenCustomizer<OAuth2TokenClaimsContext> {

    }

    /**
     * jwt token customizer ..
     */
    interface LightningCentralOAuth2JwtTokenCustomizer extends LightningCentralOAuth2TokenCustomizer<JwtEncodingContext> {

    }


}
