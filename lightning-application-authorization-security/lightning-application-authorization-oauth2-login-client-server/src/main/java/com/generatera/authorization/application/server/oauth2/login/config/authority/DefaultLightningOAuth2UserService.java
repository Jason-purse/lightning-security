package com.generatera.authorization.application.server.oauth2.login.config.authority;

import com.generatera.authorization.application.server.oauth2.login.config.user.OAuth2UserDetails;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
/**
 * @author FLJ
 * @date 2023/1/29
 * @time 16:16
 * @Description 进行伪装
 */
public class DefaultLightningOAuth2UserService implements LightningOAuth2UserService{
    private final OAuth2UserService<OAuth2UserRequest,OAuth2User> delegate;
    public DefaultLightningOAuth2UserService(OAuth2UserService<OAuth2UserRequest,OAuth2User> oAuth2UserService) {
        this.delegate = oAuth2UserService;
    }
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2AccessToken accessToken = userRequest.getAccessToken();
        Instant expiresAt = accessToken.getExpiresAt();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        return new OAuth2UserDetails(oAuth2User,accessToken.getIssuedAt(),expiresAt);
    }
}
