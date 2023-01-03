package com.generatera.authorization.server.oauth2.configuration;

import lombok.Data;
@Data
public class AuthorizationServerOAuth2CommonComponentsProperties {

    private final AuthorizationStoreComponentConfig authorizationStore = new AuthorizationStoreComponentConfig();

    private final AuthorizationConsentComponentConfig authorizationConsentStore = new AuthorizationConsentComponentConfig();

    public enum StoreKind {
        REDIS,
        JPA,
        MONGO
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
        private StoreKind kind = StoreKind.REDIS;


        private final Redis redis = new Redis("lightning-auth-server-authorization-");


    }

    @Data
    public static class AuthorizationConsentComponentConfig {
        /**
         * 默认使用redis
         */
        private StoreKind kind = StoreKind.REDIS;

        private final Redis redis = new Redis("lightning-auth-server-authorization-consent-");
    }
}
