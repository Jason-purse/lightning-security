package com.generatera.authorization.application.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * 应用级别的 认证属性
 */
@Data
@ConfigurationProperties(prefix = "lightning.auth.app.server.config")
public class ApplicationAuthServerProperties {


    public final Permission permission = new Permission();


    @Data
    public static class Permission {

        /**
         * url 白名单 - 放行,不需要token 校验
         */
        private List<String> urlWhiteList = Collections.emptyList();
    }
}
