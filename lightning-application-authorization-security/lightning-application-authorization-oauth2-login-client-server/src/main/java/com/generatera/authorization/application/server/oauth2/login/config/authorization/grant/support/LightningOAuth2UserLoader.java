package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.config.token.AuthorizationRequestAuthentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Oauth2 user {@link org.springframework.security.oauth2.core.user.OAuth2User} 获取加载器
 *
 * @see PasswordGrantAccessTokenDaoAuthenticationProvider#userService
 */
public interface LightningOAuth2UserLoader {

    OAuth2User load(AuthorizationRequestAuthentication authentication, OAuth2AccessTokenResponse auth2AccessTokenResponse);
}
