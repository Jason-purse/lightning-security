package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 授权请求配置
 *
 * 可以放入Redis中,减少jvm 内存压力 ..
 */
public class ApplicationAuthorizationRequestConfiguration {


    public static class RedisAuthorizationRequestConfiguration {

        @Bean
        public LightningAuthorizationRequestRepository authorizationRequestRepository(StringRedisTemplate redisTemplate, OAuth2LoginProperties properties) {
            return new RedisAuthorizationRequestRepository(redisTemplate,
                    properties.getAuthorizationRequestEndpoint().getStoreTimeOfMillis());
        }
    }


    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.authorization.request")
    public static class JpaAuthorizationRequestConfiguration {
        @Bean
        public LightningAuthorizationRequestRepository authorizationRequestRepository(JpaInternalAuthorizationRequestRepository repository) {
            return new JpaAuthorizationRequestRepository(repository);
        }
    }

    public static class MongoAuthorizationRequestConfiguration {
        @Bean
        public LightningAuthorizationRequestRepository authorizationRequestRepository(MongoTemplate mongoTemplate) {
            return new MongoAuthorizationRequestRepository(mongoTemplate);
        }
    }

}
