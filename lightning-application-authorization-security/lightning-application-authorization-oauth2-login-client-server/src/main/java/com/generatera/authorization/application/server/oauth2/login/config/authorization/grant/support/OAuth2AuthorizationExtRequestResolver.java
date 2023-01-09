package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;

public interface OAuth2AuthorizationExtRequestResolver {
    @Nullable
    OAuth2AuthorizationExtRequest resolve(HttpServletRequest request);

    OAuth2AuthorizationExtRequest resolve(HttpServletRequest request, String clientRegistrationId);
}
