package com.generatera.authorization.application.server.config;

import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * 应用级别的 认证属性
 */
@Data
@ConfigurationProperties(prefix = "lightning.auth.app.server.config")
public class ApplicationAuthServerProperties {

    public final AuthKind formLogin = AuthKind.enable();

    public final AuthKind oauth2Login = AuthKind.enable();

    public final AuthKind lcdpLogin = AuthKind.enable();

    /**
     * oauth2 server 服务器启用
     */
    public final AuthKind OA2AuthServer = AuthKind.enable();

    public final Permission permission = new Permission();

    public final Specification specification = new Specification();


    /**
     * 标识授权种类
     */
    public static class AuthKind {
        /**
         * 是否启用
         */
        @Setter
        private Boolean enable = false;

        public static AuthKind enable() {
            return new AuthKind(Boolean.TRUE);
        }

        public static AuthKind disable() {
            return new AuthKind(Boolean.FALSE);
        }

        public AuthKind(Boolean enable) {
            this.enable = enable;
        }

        public Boolean isEnable() {
            return enable;
        }
    }


    @Data
    public static class Permission {

        /**
         * url 白名单 - 放行,不需要token 校验
         */
        private List<String> urlWhiteList = Collections.emptyList();
    }

    @Data
    public static class Specification {

        private AuthenticationTokenSetting authenticationTokenSetting = new AuthenticationTokenSetting();


        @Data
        public static class AuthenticationTokenSetting {

            private Boolean enable = Boolean.TRUE;

            private StoreKind authenticationTokenStoreKind = StoreKind.MEMORY;

            private final Redis redis = new Redis("lightning.app.auth.server.authentication.token.");

            public Boolean getEnable() {
                return enable != null ? enable : Boolean.FALSE;
            }
        }

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


    public enum StoreKind {
        REDIS,
        JPA,
        MONGO,
        MEMORY
    }

}
