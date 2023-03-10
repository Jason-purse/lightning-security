package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store;

import com.generatera.central.oauth2.authorization.server.configuration.OAuth2CentralAuthorizationServerProperties;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.repository.OAuth2AuthorizationRepository;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.service.*;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 13:52
 * @Description 颁发的token 存储组件配置 ..
 */
@Configuration
@AutoConfiguration
public class AuthorizationStoreConfiguration {

    public static class RedisOAuth2AuthorizationStoreConfig {
        @Bean
        public OAuth2AuthorizationService oAuth2AuthorizationService(StringRedisTemplate redisTemplate,
                                                                     LightningUserPrincipalConverter userPrincipalConverter,
                                                                     OAuth2CentralAuthorizationServerProperties properties) {
            return new DefaultOpaqueSupportOAuth2AuthorizationService(
                    new RedisOAuth2AuthorizationService(redisTemplate,
                            properties.getAuthorizationStore().getRedis().getKeyPrefix(),
                            properties.getAuthorizationStore().getRedis().getExpiredTimeDuration(),
                            userPrincipalConverter)
            );
        }
    }

    @EnableJpaRepositories
    public static class JpaOAuth2AuthorizationStoreConfig {

        @Bean
        public OAuth2AuthorizationService oAuth2AuthorizationService(
                OAuth2AuthorizationRepository oAuth2AuthorizationRepository,
                RegisteredClientRepository registeredClientRepository) {
            return new DefaultOpaqueSupportOAuth2AuthorizationService(
                    new JpaOAuth2AuthorizationService(
                            oAuth2AuthorizationRepository, registeredClientRepository));
        }
    }

    public static class MongoOAuth2AuthorizationStoreConfig {
        @Bean
        public OAuth2AuthorizationService oAuth2AuthorizationService(MongoTemplate mongoTemplate) {
            return new DefaultOpaqueSupportOAuth2AuthorizationService(
                    new MongoOAuth2AuthorizationService(mongoTemplate)
            );
        }
    }

    public static class MemoryOAuth2AuthorizationStoreConfig {
        @Bean
        public OAuth2AuthorizationService oAuth2AuthorizationService() {
            return new DefaultOpaqueSupportOAuth2AuthorizationService(
                    new DefaultOAuth2AuthorizationService()
            );
        }
    }
}
