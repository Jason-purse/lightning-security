package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.config.token.AuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.token.PasswordGrantAuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class DefaultPasswordGrantAccessTokenDaoAuthenticationProvider extends PasswordGrantAccessTokenDaoAuthenticationProvider {
    public DefaultPasswordGrantAccessTokenDaoAuthenticationProvider(LightningOAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> auth2AccessTokenResponseClient, LightningOAuth2UserService userService, LightningOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository) {
        super(auth2AccessTokenResponseClient,
                new LightningOAuth2UserLoader() {
                    @Override
                    public OAuth2User load(AuthorizationRequestAuthentication authentication, OAuth2AccessTokenResponse auth2AccessTokenResponse) {
                        PasswordGrantAuthorizationRequestAuthentication requestAuthentication = (PasswordGrantAuthorizationRequestAuthentication) authentication;
                        return userService.loadUser(new OAuth2UserRequest(requestAuthentication.getClientRegistration(), auth2AccessTokenResponse.getAccessToken(), auth2AccessTokenResponse.getAdditionalParameters()));
                    }
                }, auth2AuthorizedClientRepository);
    }

}
