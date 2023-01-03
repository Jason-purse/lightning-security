package com.generatera.authorization.server.common.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightning.auth.server.common.component")
public class AuthorizationServerComponentProperties {

    private final AuthorizationStoreComponentConfig authorizationStore = new AuthorizationStoreComponentConfig();

    private final AuthorizationConsentComponentConfig authorizationConsentStore = new AuthorizationConsentComponentConfig();

    private final ProviderSettingProperties providerSettingProperties = new ProviderSettingProperties();

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

    public enum  StoreKind {
        REDIS,
        JPA,
        MONGO
    }

    @Data
    public static class Redis {
        /**
         * key 前缀
         */
        private final  String DEFAULT_KEY_PREFIX;

        private String keyPrefix = DEFAULT_KEY_PREFIX;

        /**
         *  value 保留 30分钟(但是不能够使用恰好30分钟,需要比正常时间少10 - 30秒)
         */
        private final Long DEFAULT_EXPIRED_TIME = 30 * 1000 * 60L;


        private Long expiredTimeDuration = DEFAULT_EXPIRED_TIME;

        /**
         * 需要比正常时间少10 - 30秒
         * @return expired_time
         */
        public Long getExpiredTimeDuration() {
            return expiredTimeDuration - 10 * 1000L;
        }

        public static Redis of(String keyPrefix) {
            return new Redis(keyPrefix);
        }
    }


    @Data
    public static class ProviderSettingProperties {

        // 可以为空(自己自动生成)
        private String issuer;

        private String authorizationEndpoint = "/auth/v1/oauth2/authorize";

        private String tokenEndpoint = "/auth/v1/oauth2/token";

        private String jwkSetEndpoint = "/auth/v1/oauth2/jwks";

        private String tokenRevocationEndpoint = "/auth/v1/oauth2/revoke";

        private String tokenIntrospectionEndpoint = "/auth/v1/oauth2/introspect";

        private String oidcClientRegistrationEndpoint = "/auth/v1/connect/register";

        private String oidcUserInfoEndpoint = "/auth/v1/userinfo";
    }

}
