package com.generatera.authorization.application.server.oauth2.login.config.authority;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * oauth2 user service
 */
public interface LightningOAuth2UserService extends OAuth2UserService<OAuth2UserRequest,OAuth2User> {


}
