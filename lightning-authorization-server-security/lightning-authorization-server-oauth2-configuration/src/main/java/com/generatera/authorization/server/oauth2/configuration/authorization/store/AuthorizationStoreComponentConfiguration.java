package com.generatera.authorization.server.oauth2.configuration.authorization.store;

import com.generatera.authorization.server.oauth2.configuration.AuthorizationServerOAuth2CommonComponentsProperties;
import com.generatera.authorization.server.oauth2.configuration.authorization.store.repository.OAuth2AuthorizationRepository;
import com.generatera.authorization.server.oauth2.configuration.authorization.store.service.JpaOAuth2AuthorizationService;
import com.generatera.authorization.server.oauth2.configuration.authorization.store.service.MongoOAuth2AuthorizationService;
import com.generatera.authorization.server.oauth2.configuration.authorization.store.service.RedisOAuth2AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public class AuthorizationStoreComponentConfiguration {


    public static class OAuth2AuthorizationStoreConfig {

        @Configuration
        public static class RedisOAuth2AuthorizationStoreConfig {
            @Bean
            public OAuth2AuthorizationService oAuth2AuthorizationService(StringRedisTemplate redisTemplate,
                                                                         AuthorizationServerOAuth2CommonComponentsProperties properties) {
                return new RedisOAuth2AuthorizationService(redisTemplate,properties);
            }
        }


        @EnableJpaRepositories(basePackages = "com.generatera.authorization.server.oauth2.configuration.repository")
        public static class JpaOAuth2AuthorizationStoreConfig {

            @Bean
            public OAuth2AuthorizationService oAuth2AuthorizationService(OAuth2AuthorizationRepository oAuth2AuthorizationRepository, RegisteredClientRepository registeredClientRepository) {
                return new JpaOAuth2AuthorizationService(oAuth2AuthorizationRepository, registeredClientRepository);
            }
        }

        public static class MongoOAuth2AuthorizationStoreConfig {
            @Bean
            public OAuth2AuthorizationService oAuth2AuthorizationService(MongoTemplate mongoTemplate) {
                return new MongoOAuth2AuthorizationService(mongoTemplate);
            }
        }
    }
}
