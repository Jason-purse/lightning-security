package com.generatera.authorization.application.server.oauth2.login.config.authority;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * open connect id user service
 */
public interface LightningOidcUserService extends OAuth2UserService<OidcUserRequest, OidcUser> {
}
