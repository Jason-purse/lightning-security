package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.Redis;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@AutoConfiguration
public class AuthorizationStoreConfiguration {

    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.config.authorization.store.dao")
    @EntityScan(basePackages = "com.generatera.authorization.application.server.config.model.entity")
    public static class JpaStoreConfiguration {
        @Bean
        public LightningAuthenticationTokenService authenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter) {
            return new JpaAuthenticationTokenService(userPrincipalConverter);
        }
    }

    @EnableMongoRepositories(basePackages = "com.generatera.authorization.application.server.config.authorization.store.dao")
    @EntityScan(basePackages = "com.generatera.authorization.application.server.config.model.entity")
    public static class MongoStoreConfiguration {
        @Bean
        public LightningAuthenticationTokenService authenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter) {
            return new MongoAuthenticationTokenService(userPrincipalConverter);
        }
    }

    public static class MemoryStoreConfiguration {
        @Bean
        public LightningAuthenticationTokenService authorizationService(@Autowired(required = false) LightningUserPrincipalConverter userPrincipalConverter) {
            DefaultAuthenticationTokenService authenticationTokenService = new DefaultAuthenticationTokenService();
            if (userPrincipalConverter != null) {
                authenticationTokenService.setTokenConverter(new OptimizedAuthenticationTokenConverter(userPrincipalConverter));
            }
            return authenticationTokenService;
        }
    }

    public static class RedisStoreConfiguration {
        @Bean
        public LightningAuthenticationTokenService authenticationTokenService(LightningUserPrincipalConverter userPrincipalConverter, ApplicationAuthServerProperties properties) {
            Redis redis = properties.getAuthorizationStoreConfig().getRedis();
            return new RedisAuthenticationTokenService(redis.getKeyPrefix(), redis.getExpiredTimeDuration(), userPrincipalConverter);
        }
    }


}
