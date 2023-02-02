package com.generatera.authorization.application.server.oauth2.login.config.authority;

import com.generatera.authorization.application.server.oauth2.login.config.user.OidcUserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class DefaultLightningOidcUserService implements LightningOidcUserService {
    private final OAuth2UserService<OidcUserRequest,OidcUser> delegate;
    public DefaultLightningOidcUserService(OAuth2UserService<OidcUserRequest,OidcUser> oidcUserService) {
        this.delegate = oidcUserService;
    }
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return new OidcUserDetails(delegate.loadUser(userRequest));
    }
}
