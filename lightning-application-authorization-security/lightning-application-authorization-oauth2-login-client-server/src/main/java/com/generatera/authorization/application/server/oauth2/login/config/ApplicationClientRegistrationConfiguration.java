package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.client.register.JpaClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.repository.client.registration.JpaInternalClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.register.MongoClientRegistrationRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * in memory 形式 spring 自动注入 ...
 */
@AutoConfiguration
public class ApplicationClientRegistrationConfiguration {

    public static class MongoClientRegistrationConfiguration {

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository(MongoTemplate mongoTemplate) {
            return new MongoClientRegistrationRepository(mongoTemplate);
        }
    }

    @EntityScan(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration")
    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.repository.client.registration")
    public static class JPAClientRegistrationConfiguration {

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository(JpaInternalClientRegistrationRepository repository) {
            return new JpaClientRegistrationRepository(repository);
        }
    }

}
