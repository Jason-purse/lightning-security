package com.generatera.authorization.application.server.config.specification.authorization.store;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.repository.JpaAuthenticationTokenRepository;
import com.generatera.authorization.application.server.config.specification.LightningAuthenticationTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

public class AuthenticationTokenComponentConfiguration {

    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.config.repository")
    public static class JpaAuthenticationTokenComponentConfiguration {
        @Bean
        @ConditionalOnMissingBean(LightningAuthenticationTokenService.class)
        public JpaAuthenticationTokenService jpaAuthenticationTokenService(JpaAuthenticationTokenRepository jpaAuthenticationTokenRepository) {
            return new JpaAuthenticationTokenService(jpaAuthenticationTokenRepository);
        }
    }


    public static class MongoAuthenticationTokenComponentConfiguration {
        @Bean
        @ConditionalOnMissingBean(LightningAuthenticationTokenService.class)
        public MongoAuthenticationTokenService mongoAuthenticationTokenService(MongoTemplate mongoTemplate) {
            return new MongoAuthenticationTokenService(mongoTemplate);
        }
    }

    public static class RedisAuthenticationTokenComponentConfiguration {
        @Bean
        @ConditionalOnMissingBean(LightningAuthenticationTokenService.class)
        public RedisAuthenticationTokenService redisAuthenticationTokenService(StringRedisTemplate redisTemplate,
                                                                               ApplicationAuthServerProperties properties) {
            return new RedisAuthenticationTokenService(
                    redisTemplate,
                    properties
                            .getSpecification()
                            .getAuthenticationTokenSetting().getRedis()
                            .getKeyPrefix(),
                    properties
                            .getSpecification()
                            .getAuthenticationTokenSetting()
                            .getRedis().getExpiredTimeDuration()
            );
        }
    }

    public static class MemoryAuthenticationTokenComponentConfiguration {
        @Bean
        @ConditionalOnMissingBean(LightningAuthenticationTokenService.class)
        public DefaultAuthenticationTokenService authenticationTokenService() {
            return new DefaultAuthenticationTokenService();
        }
    }
}
