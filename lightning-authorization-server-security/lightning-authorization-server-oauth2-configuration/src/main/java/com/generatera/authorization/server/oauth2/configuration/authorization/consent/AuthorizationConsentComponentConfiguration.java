package com.generatera.authorization.server.oauth2.configuration.authorization.consent;

import com.generatera.authorization.server.oauth2.configuration.repository.OAuth2AuthorizationConsentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * 授权协商组件配置
 */
public class AuthorizationConsentComponentConfiguration {

    public static class JpaAuthorizationConsentComponentConfiguration {
        @Bean
        @ConditionalOnMissingBean(OAuth2AuthorizationConsentService.class)
        public OAuth2AuthorizationConsentService authorizationConsentService(OAuth2AuthorizationConsentRepository oauth2AuthorizationConsentRepository, RegisteredClientRepository registeredClientRepository) {
            return new JpaOAuth2AuthorizationConsentService(oauth2AuthorizationConsentRepository, registeredClientRepository);
        }
    }
}
