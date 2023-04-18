package com.generatera.central.oauth2.authorization.server.configuration;

import com.generatera.central.oauth2.authorization.server.configuration.components.provider.OAuth2ProviderSettingProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 主要是 OAuth2 中央授权服务器属性
 * 包括以下部分:
 * 1. 授权信息存储配置
 * 2. 授权协商记录存储配置
 * 3. 客户端信息存储配置(todo)
 *
 *
 * 中央授权服务器, 此框架基于 OAUTH2 作为中央授权服务器规范 ..
 *
 * 其他的授权方案,不属于中央授权服务器规范的一部分,仅仅是一种授权方案 ..
 */
@Data
@ConfigurationProperties(prefix = "lightning.security.central.oauth2.auth.server")
public class OAuth2CentralAuthorizationServerProperties {

    /**
     * 授权存储组件信息配置
     */
    private final AuthorizationStoreComponentConfig authorizationStore = new AuthorizationStoreComponentConfig();

    /**
     * 授权协商组件信息配置
     */
    private final AuthorizationConsentComponentConfig authorizationConsentStore = new AuthorizationConsentComponentConfig();

    /**
     * oauth2 提供器配置属性
     */
    private final OAuth2ProviderSettingProperties provider = new OAuth2ProviderSettingProperties();


    /**
     * 表单登录配置支持(oauth2 流程中需要用户抉择是否授权给第三方)
     */
    private final FormLoginSupportConfig formLoginConfig = new FormLoginSupportConfig();

    @Data
    public static class FormLoginSupportConfig {

        private String loginPageUrl;

        private String loginProcessUrl = "/auth/v1/form/login";

        /**
         * 默认配置,当没有之前的请求时使用 ..
         */
        private String defaultSuccessForwardUrl = "/default_success.html";

        /**
         * 成功之后的转发url
         */
        private String successForwardUrl = "/default_success.html";

        /**
         * 成功之后的 重定向url
         */
        private String successRedirectUrl;

        /**
         * 设置默认的成功重定向地址 ...
         */
        private String defaultSuccessRedirectUrl;

    }

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


        private final Redis redis = new Redis("lightning-central-oauth2-server-authorization-");


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
