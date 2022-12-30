package com.generatera.authorization.server.configure;

import com.generatera.authorization.server.configure.client.*;
import com.generatera.authorization.server.configure.store.authorizationinfo.JpaOAuth2AuthorizationService;
import com.generatera.authorization.server.configure.store.authorizationinfo.MongoOAuth2AuthorizationService;
import com.generatera.authorization.server.configure.store.authorizationinfo.OAuth2AuthorizationProperties;
import com.generatera.authorization.server.configure.store.authorizationinfo.RedisOAuth2AuthorizationService;
import com.generatera.authorization.server.configure.store.authorizationinfo.repository.OAuth2AuthorizationRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * @author FLJ
 * @date 2022/12/29
 * @time 14:48
 * @Description OAuth2 Server 基本配置
 */
@AutoConfigureAfter({RedisAutoConfiguration.class, MongoAutoConfiguration.class, DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@Configuration
public class OAuthServerComponentConfiguration {


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


    @Configuration
    @EnableConfigurationProperties(OAuth2AuthorizationProperties.class)
    public static class OAuth2AuthorizationStoreConfig {

        @Configuration
        @ConditionalOnProperty(prefix = "lightning.auth.server.authorization.store",name = "kind",havingValue = "REDIS",matchIfMissing = true)
        public static class RedisOAuth2AuthorizationStoreConfig {
            @Bean
            public OAuth2AuthorizationService oAuth2AuthorizationService(StringRedisTemplate redisTemplate,
                                                                         OAuth2AuthorizationProperties properties) {
                return new RedisOAuth2AuthorizationService(redisTemplate,properties);
            }
        }


        @Configuration
        @ConditionalOnProperty(prefix = "lightning.auth.server.authorization.store",name = "kind",havingValue = "JPA")
        @EnableJpaRepositories(basePackages = "com.generatera.authorization.server.configure.store.authorizationinfo.repository")
        public static class JpaOAuth2AuthorizationStoreConfig {

            @Bean
            public OAuth2AuthorizationService oAuth2AuthorizationService(OAuth2AuthorizationRepository oAuth2AuthorizationRepository, RegisteredClientRepository registeredClientRepository) {
                return new JpaOAuth2AuthorizationService(oAuth2AuthorizationRepository, registeredClientRepository);
            }
        }

        @Configuration
        @ConditionalOnProperty(prefix = "lightning.auth.server.authorization.store",name = "kind",havingValue = "MONGO")
        public static class MongoOAuth2AuthorizationStoreConfig {
            @Bean
            public OAuth2AuthorizationService oAuth2AuthorizationService(MongoTemplate mongoTemplate) {
                return new MongoOAuth2AuthorizationService(mongoTemplate);
            }
        }
    }


}
