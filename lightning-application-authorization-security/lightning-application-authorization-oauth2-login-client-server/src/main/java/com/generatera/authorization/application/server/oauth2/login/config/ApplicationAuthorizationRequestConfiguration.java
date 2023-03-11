package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfigurer;
import com.generatera.authorization.application.server.config.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2LoginExtUtils;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.*;
import com.generatera.authorization.application.server.oauth2.login.config.repository.authorization.request.JpaInternalAuthorizationRequestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 授权请求配置
 * <p>
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


    public static class DefaultAuthorizationRequestConfiguration {
        @Bean
        public LightningAuthorizationRequestRepository authorizationRequestRepository() {
            return new DefaultAuthorizationRequestRepository();
        }
    }

    /**
     * 普通配置 ..
     */
    public static class AuthorizationRequestCommonConfiguration {

        @Bean
        public LightningAppAuthServerConfigurer commonOAuth2ConfigAppAuthServer(OAuth2LoginProperties properties) {
            return new LightningAppAuthServerConfigurer() {
                @Override
                public void configure(ApplicationAuthServerConfigurer<HttpSecurity> applicationAuthServerConfigurer) throws Exception {

                    applicationAuthServerConfigurer.tokenEndpoint(token -> {
                        // 替代
                        token.addAccessTokenRequestConverter(OAuth2LoginExtUtils.getPasswordGrantAuthenticationRequestConverter(applicationAuthServerConfigurer.and()));
                        // 设置 dao 提供器 ..
                        token.authenticationDaoProvider(
                                OAuth2LoginExtUtils.getOAuth2ExtAuthenticationDaoProvider(applicationAuthServerConfigurer.and())
                        );
                    });

                }
            };
        }
    }

}
