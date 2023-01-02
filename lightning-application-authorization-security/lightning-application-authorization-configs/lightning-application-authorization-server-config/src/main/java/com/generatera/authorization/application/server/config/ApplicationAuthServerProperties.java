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


    public final Permission permission = new Permission();



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

}
