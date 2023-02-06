package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.client.register.*;
import com.generatera.authorization.application.server.oauth2.login.config.repository.client.registration.JpaInternalClientRegistrationRepository;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * in memory 形式 spring 自动注入 ...
 */
@Configuration
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

    /**
     * 一个 坑记录 ..
     * 这些配置都属于自动配置,如果被componentsScan 提前扫描到,则将会存在bean 提前注入的情况,这可能是错误的 ..
     *
     * 由于组件扫描同样会扫描内嵌类,所以 要么不添加@Configuration 注解,否则
     * 同时添加@Configuration 以及 @AutoConfiguration 注解(来逃避 候选选择) ...
     */
    public static class DefaultClientRegistrationConfiguration {

        /**
         * 如果配置, 则拿取
         */
        @Bean
        @Conditional({ClientsConfiguredCondition.class})
        @Order(Ordered.HIGHEST_PRECEDENCE + 2)
        public LightningAuthServerConfigurer registrationRepositoryHandle(ClientRegistrationRepository clientRegistrationRepository) {
            LightningOAuth2ClientRegistrationRepository oAuth2ClientRegistrationRepository = new DelegateClientRegistrationRepository(clientRegistrationRepository);

            return new LightningAuthServerConfigurer() {
                @Override
                public void configure(HttpSecurity securityBuilder) throws Exception {
                    securityBuilder.setSharedObject(LightningOAuth2ClientRegistrationRepository.class, oAuth2ClientRegistrationRepository);
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(ClientRegistrationRepository.class)
        public LightningOAuth2ClientRegistrationRepository fillRegistrationRepository() {
            return new DefaultClientRegistrationRepository();
        }


    }

}
