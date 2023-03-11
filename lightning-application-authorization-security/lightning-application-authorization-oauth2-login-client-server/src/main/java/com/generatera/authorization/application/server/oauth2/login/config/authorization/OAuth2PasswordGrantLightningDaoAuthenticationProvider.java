package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.authorization.application.server.config.token.LightningDaoAuthenticationProvider;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.token.DefaultAuthorizationRequestAuthentication;
import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * @author FLJ
 * @date 2023/3/7
 * @time 13:38
 * @Description 支持 client secret 直接进行用户信息交换并产生token
 */
@RequiredArgsConstructor
public class OAuth2PasswordGrantLightningDaoAuthenticationProvider implements LightningDaoAuthenticationProvider {

    private final OAuth2AuthorizedClientManager clientManager;

    private final LightningOAuth2UserService oAuth2UserService;

    @Override
    public Authentication authenticate(AuthAccessTokenAuthenticationToken authAccessTokenAuthenticationToken) {
        // 支持 client secret 处理
        if (authAccessTokenAuthenticationToken.getAuthentication() instanceof DefaultAuthorizationRequestAuthentication authentication) {
            // 处理 ..
            OAuth2AuthorizedClient client = clientManager.authorize(
                    OAuth2AuthorizeRequest
                            .withClientRegistrationId(authentication.getClientId())
                            .principal(
                                    new UsernamePasswordAuthenticationToken(
                                            DefaultLightningUserDetails
                                                    .withUsername(authentication.getAdditionalParameters().get(OAuth2ParameterNames.USERNAME).toString())
                                                    .password(authentication.getAdditionalParameters().get(OAuth2ParameterNames.PASSWORD).toString())
                                                    .build(), null
                                    )
                            )
                            .build()
            );
            if (client == null) {
                throw new InternalAuthenticationServiceException("invalid client");
            }
            OAuth2User user = loadOAuth2User(client, authentication);
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        return null;
    }

    protected OAuth2User loadOAuth2User(OAuth2AuthorizedClient client, DefaultAuthorizationRequestAuthentication authentication) {
        return oAuth2UserService.loadUser(new OAuth2UserRequest(client.getClientRegistration(), client.getAccessToken()));
    }
}
