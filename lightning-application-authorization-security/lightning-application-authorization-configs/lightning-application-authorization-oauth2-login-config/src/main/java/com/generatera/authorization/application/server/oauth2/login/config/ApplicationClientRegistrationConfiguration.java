package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.client.JpaClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.JpaInternalClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.MongoClientRegistrationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * in memory 形式 spring 自动注入 ...
 */
@Configuration
public class ApplicationClientRegistrationConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "lightning.auth.app.server.oauth2.login.config",name = "client-registration-store-kind",value = "MONGO")
    public static class MongoClientRegistrationConfiguration {

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository(MongoTemplate mongoTemplate) {
            return new MongoClientRegistrationRepository(mongoTemplate);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "lightning.auth.app.server.oauth2.login.config",name = "client-registration-store-kind",value = "JPA")
    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.client")
    public static class JPAClientRegistrationConfiguration {

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository(JpaInternalClientRegistrationRepository repository) {
            return new JpaClientRegistrationRepository(repository);
        }
    }

}
