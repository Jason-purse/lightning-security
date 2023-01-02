package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;

public class ApplicationAuthorizedClientConfiguration {

    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.client")
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



    @Bean
    @ConditionalOnMissingBean(LightningAnonymousOAuthorizedClientRepository.class)
    public LightningAnonymousOAuthorizedClientRepository anonymousOAuthorizedClientRepository() {
        return new DefaultEmptyAnonymousOAuthorizedClientService();
    }

}
