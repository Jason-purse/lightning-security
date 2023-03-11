package com.generatera.authorization.application.server.oauth2.login.config.authority;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
/**
 * @author FLJ
 * @date 2023/3/10
 * @time 17:24
 * @Description 执行器 ...OAuth2PasswordGrantLightningDaoAuthenticationProvider
 */
public interface LightningOAuth2UserServiceInvoker<T extends OAuth2UserRequest,User extends OAuth2User> {
    User internalLoadUser(T userRequest) throws OAuth2AuthenticationException;
}
