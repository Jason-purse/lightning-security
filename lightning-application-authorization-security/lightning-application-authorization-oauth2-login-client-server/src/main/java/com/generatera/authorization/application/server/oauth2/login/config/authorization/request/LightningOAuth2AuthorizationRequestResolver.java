package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.LightningOAuth2AuthorizationExtRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

/**
 * 扩展点 ..
 * 扩展Oauth2 authorization request resolver
 *
 * 暂时 {@link  org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest} 无法扩展
 *
 * 请使用{@link com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2AuthorizationExtRequest}
 *  进行扩展 ..
 *
 * @see LightningOAuth2AuthorizationExtRequestResolver
 */
public interface LightningOAuth2AuthorizationRequestResolver extends OAuth2AuthorizationRequestResolver {
}
