package com.generatera.authorization.server.oauth2.configuration.client;

import com.generatera.authorization.server.oauth2.configuration.authorization.consent.JpaOAuth2AuthorizationConsentService;
import com.generatera.authorization.server.oauth2.configuration.repository.OAuth2AuthorizationConsentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class RegisteredClientConfiguration {
    /**
     * 也可以自己 实现 client repository ..
     */
    @Configuration
    @ConditionalOnMissingBean(RegisteredClientRepository.class)
    public static class ClientRepositoryConfig {



        @Bean
        public RegisteredClientRepository registeredClientRepository(AppService appService) {
            return new LightningRegisteredClientRepository(appService);
        }

        @Bean
        public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(OAuth2AuthorizationConsentRepository oAuth2AuthorizationConsentRepository, RegisteredClientRepository registeredClientRepository) {
            return new JpaOAuth2AuthorizationConsentService(oAuth2AuthorizationConsentRepository,registeredClientRepository);
        }

        @Bean
        public AppService appService() {
            return new DefaultAppServiceImpl();
        }

        // for appService ...

        @Bean
        public DefaultAppQueryHandler defaultAppQueryHandler() {
            return new DefaultAppQueryHandler();
        }

        @Bean
        public AppParamConverter appParamConverter() {
            return new AppParamConverter();
        }
    }
}
