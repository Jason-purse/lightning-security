package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.central.oauth2.authorization.server.configuration.OAuth2CentralAuthorizationServerProperties;
import com.generatera.central.oauth2.authorization.server.configuration.repository.authorization.store.OAuth2AuthorizationRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
        public LightningAuthorizationService<DefaultOAuth2Authorization> oAuth2AuthorizationService(StringRedisTemplate redisTemplate,
                                                                                                    OAuth2CentralAuthorizationServerProperties properties) {
            return new RedisOAuth2AuthorizationService(redisTemplate,properties);
        }
    }

    @EnableJpaRepositories(basePackages = "com.generatera.central.oauth2.authorization.server.configuration.repository.authorization.store")
    @EntityScan(basePackages = "com.generatera.central.oauth2.authorization.server.configuration.model.entity.authorization")
    public static class JpaOAuth2AuthorizationStoreConfig {

        @Bean
        public LightningAuthorizationService<DefaultOAuth2Authorization> oAuth2AuthorizationService(OAuth2AuthorizationRepository oAuth2AuthorizationRepository, RegisteredClientRepository registeredClientRepository) {
            return new JpaOAuth2AuthorizationService(oAuth2AuthorizationRepository, registeredClientRepository);
        }
    }

    public static class MongoOAuth2AuthorizationStoreConfig {
        @Bean
        public LightningAuthorizationService<DefaultOAuth2Authorization> oAuth2AuthorizationService(MongoTemplate mongoTemplate) {
            return new MongoOAuth2AuthorizationService(mongoTemplate);
        }
    }
}
