package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

/**
 * oauth2 authorization request ext(目前不支持 authorization_code 之外的请求体构建)
 */
public interface LightningOAuth2AuthorizationExtRequestResolver {
    @Nullable
    OAuth2AuthorizationExtRequest resolve(HttpServletRequest request);

    OAuth2AuthorizationExtRequest resolve(HttpServletRequest request, String clientRegistrationId);
}
