package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.*;
import com.generatera.authorization.application.server.oauth2.login.config.repository.client.authorized.JpaOAuthorizedClientRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

public class ApplicationAuthorizedClientConfiguration {

    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.repository.client.authorized")
    public static class JpaAuthorizedClientConfiguration {

        @Bean
        public LightningOAuthorizedClientService lightningOAuthorizedClientService(
                JpaOAuthorizedClientRepository repository
        ) {
            return new JpaOAuthorizedClientService(repository);
        }

    }

    public static class MongoAuthorizedClientConfiguration {
        @Bean
        public LightningOAuthorizedClientService lightningOAuthorizedClientService(MongoTemplate mongoTemplate) {
            return new MongoOAuthorizedClientService(mongoTemplate);
        }
    }

    public static class DefaultAuthorizedClientConfiguration {
        @Bean
        public LightningOAuthorizedClientService memoryOAuthorizedClientService(OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
            return new DelegateOAuthorizedClientService(oAuth2AuthorizedClientService);
        }
    }



    @Bean
    @ConditionalOnMissingBean(LightningAnonymousOAuthorizedClientRepository.class)
    public LightningAnonymousOAuthorizedClientRepository anonymousOAuthorizedClientRepository() {
        return new DefaultEmptyAnonymousOAuthorizedClientService();
    }

}
