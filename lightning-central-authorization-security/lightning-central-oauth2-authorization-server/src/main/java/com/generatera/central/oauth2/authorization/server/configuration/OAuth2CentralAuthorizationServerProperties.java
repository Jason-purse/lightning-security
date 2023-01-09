package com.generatera.central.oauth2.authorization.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 主要是 OAuth2 中央授权服务器属性
 * 包括以下部分:
 * 1. 授权信息存储配置
 * 2. 授权协商记录存储配置
 * 3. 客户端信息存储配置(todo)
 */
@Data
@ConfigurationProperties(prefix = "lightning.security.central.oauth2.auth.server")
public class OAuth2CentralAuthorizationServerProperties {

    private final AuthorizationStoreComponentConfig authorizationStore = new AuthorizationStoreComponentConfig();

    private final AuthorizationConsentComponentConfig authorizationConsentStore = new AuthorizationConsentComponentConfig();

    public enum StoreKind {
        REDIS,
        JPA,
        MONGO,
        MEMORY
    }

    @Data
    public static class Redis {

        /**
         * key 前缀
         */
        private String keyPrefix;

        /**
         * value 保留 30分钟(但是不能够使用恰好30分钟,需要比正常时间少10 - 30秒)
         */
        private final Long DEFAULT_EXPIRED_TIME = 30 * 1000 * 60L;


        private Long expiredTimeDuration = DEFAULT_EXPIRED_TIME;

        public Redis(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        /**
         * 需要比正常时间少10 - 30秒
         *
         * @return expired_time
         */
        public Long getExpiredTimeDuration() {
            return expiredTimeDuration != null ? expiredTimeDuration : DEFAULT_EXPIRED_TIME - 10 * 1000L;
        }

        public static Redis of(String keyPrefix) {
            return new Redis(keyPrefix);
        }
    }

    @Data
    public static class AuthorizationStoreComponentConfig {

        /**
         * 默认使用redis
         */
        private StoreKind kind = StoreKind.MEMORY;


        private final Redis redis = new Redis("lightning-auth-server-authorization-");


    }

    @Data
    public static class AuthorizationConsentComponentConfig {
        /**
         * 默认使用redis
         */
        private StoreKind kind = StoreKind.MEMORY;

        private final Redis redis = new Redis("lightning-auth-server-authorization-consent-");
    }
}
