package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.token.DefaultAuthorizationRequestAuthentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
/**
 * @author FLJ
 * @date 2023/3/10
 * @time 23:00
 * @Description 支持oidc dao 认证提供器 ..
 */
public class OidcPasswordGrantLightningDaoAuthenticationProvider extends OAuth2PasswordGrantLightningDaoAuthenticationProvider {
    private final LightningOidcUserService oidcUserService;
    public OidcPasswordGrantLightningDaoAuthenticationProvider(OAuth2AuthorizedClientManager clientManager,LightningOidcUserService oidcUserService) {
        super(clientManager, null);

        this.oidcUserService = oidcUserService;
    }

    @Override
    protected OAuth2User loadOAuth2User(OAuth2AuthorizedClient client, DefaultAuthorizationRequestAuthentication authentication) {
       return oidcUserService.loadUser(new OidcUserRequest(client.getClientRegistration(),client.getAccessToken(),new OidcIdToken()));
    }
}
