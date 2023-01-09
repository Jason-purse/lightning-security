package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.consent;

import com.generatera.central.oauth2.authorization.server.configuration.repository.authorization.consent.OAuth2AuthorizationConsentRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * 授权协商组件配置
 */
@Configuration
@AutoConfiguration
public class AuthorizationConsentConfiguration {

    public static class JpaAuthorizationConsentComponentConfiguration {
        @Bean
        @ConditionalOnMissingBean(OAuth2AuthorizationConsentService.class)
        public OAuth2AuthorizationConsentService authorizationConsentService(OAuth2AuthorizationConsentRepository oauth2AuthorizationConsentRepository, RegisteredClientRepository registeredClientRepository) {
            return new JpaOAuth2AuthorizationConsentService(oauth2AuthorizationConsentRepository, registeredClientRepository);
        }
    }
}
