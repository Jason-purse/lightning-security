package com.generatera.authorization.application.server.config;

import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * 应用级别的 认证属性
 */
@ConfigurationProperties(prefix = "lightning.auth.app.server.config")
public class ApplicationAuthServerProperties {

    public AuthKind formLogin = AuthKind.enable();

    public AuthKind oauth2Login = AuthKind.disable();

    public AuthKind lcdpLogin = AuthKind.disable();


    public Permission permission = new Permission();



    /**
     * 标识授权种类
     */
    public static class AuthKind {
        /**
         * 是否启用
         */
        @Setter
        private Boolean enable = false;

        public  static AuthKind enable() {
            return new AuthKind(true);
        }

        public static AuthKind disable() {
            return new AuthKind(false);
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
