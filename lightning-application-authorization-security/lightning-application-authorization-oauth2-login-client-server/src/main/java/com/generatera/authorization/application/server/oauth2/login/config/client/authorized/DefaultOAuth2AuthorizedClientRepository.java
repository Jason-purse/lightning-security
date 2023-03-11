package com.generatera.authorization.application.server.oauth2.login.config.client.authorized;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author FLJ
 * @date 2023/3/10
 * @time 16:15
 * @Description 默认实现,委派到 {@link OAuth2AuthorizedClientRepository} 上进行处理 ..
 * 这是适用于默认配置 ..
 *
 * @see com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2ClientConfigurerExtUtils#getAuthorizedClientRepository(HttpSecurityBuilder)
 */
public class DefaultOAuth2AuthorizedClientRepository implements LightningOAuth2AuthorizedClientRepository {

    private final OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    public DefaultOAuth2AuthorizedClientRepository(OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository) {
        this.oAuth2AuthorizedClientRepository = oAuth2AuthorizedClientRepository;
    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, Authentication principal, HttpServletRequest request) {
        return this.oAuth2AuthorizedClientRepository.loadAuthorizedClient(clientRegistrationId,principal,request);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal, HttpServletRequest request, HttpServletResponse response) {
        this.oAuth2AuthorizedClientRepository.saveAuthorizedClient(authorizedClient,principal,request,response);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal, HttpServletRequest request, HttpServletResponse response) {
        this.oAuth2AuthorizedClientRepository.removeAuthorizedClient(clientRegistrationId,principal,request,response);
    }
}
