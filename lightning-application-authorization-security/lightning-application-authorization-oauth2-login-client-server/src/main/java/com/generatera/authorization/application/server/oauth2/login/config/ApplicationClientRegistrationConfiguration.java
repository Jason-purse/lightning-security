package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.client.register.*;
import com.generatera.authorization.application.server.oauth2.login.config.repository.client.registration.JpaInternalClientRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        public LightningOAuth2ClientRegistrationRepository clientRegistrationRepository(MongoTemplate mongoTemplate) {
            return new MongoClientRegistrationRepository(mongoTemplate);
        }
    }

    @EntityScan(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.model.entity.registration")
    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.repository.client.registration")
    public static class JPAClientRegistrationConfiguration {

        @Bean
        public LightningOAuth2ClientRegistrationRepository clientRegistrationRepository(JpaInternalClientRegistrationRepository repository) {
            return new JpaClientRegistrationRepository(repository);
        }
    }

    public static class DefaultClientRegistrationConfiguration {

        @Bean
        public LightningOAuth2ClientRegistrationRepository lightningClientRegistrationRepository(@Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
            if (clientRegistrationRepository == null) {
                // 说明,默认配置没有生效 ...
                // 直接给出一个 默认值(这将有效的输出一个 什么也无法登录的 oauth2 client auth server服务器)
                clientRegistrationRepository = new DefaultClientRegistrationRepository();
            }


            return new DelegateClientRegistrationRepository(clientRegistrationRepository);
        }
    }

}
