package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.OAuth2LoginProperties;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.JpaAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.JpaInternalAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.RedisAuthorizationRequestRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 授权请求配置
 *
 * 可以放入Redis中,减少jvm 内存压力 ..
 */
@Configuration
public class ApplicationAuthorizationRequestConfiguration {


    @Configuration
    @ConditionalOnProperty(prefix = "lightning.auth.app.server.oauth2.login.config.authorization-request-endpoint",name = "store-kind",havingValue = "REDIS")
    public static class RedisAuthorizationRequestConfiguration {

        @Bean
        public LightningAuthorizationRequestRepository authorizationRequestRepository(StringRedisTemplate redisTemplate, OAuth2LoginProperties properties) {
            return new RedisAuthorizationRequestRepository(redisTemplate,
                    properties.getAuthorizationRequestEndpoint().getStoreTimeOfMillis());
        }
    }


    @Configuration
    @ConditionalOnProperty(prefix = "lightning.auth.app.server.oauth2.login.config.authorization-request-endpoint",name = "store-kind",havingValue = "JPA")
    @EnableJpaRepositories(basePackages = "com.generatera.authorization.application.server.oauth2.login.config.authorization.request")
    public static class JpaAuthorizationRequestConfiguration {
        @Bean
        public LightningAuthorizationRequestRepository authorizationRequestRepository(JpaInternalAuthorizationRequestRepository repository) {
            return new JpaAuthorizationRequestRepository(repository);
        }
    }

}
