package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 14:25
 * @Description lightning app auth server for token endpoint configurer ...
 */
public interface LightningAppAuthServerForTokenEndPointConfigurer {

    /**
     * 直接配置 ApplicationAuthServerConfigurer 的快捷方式 ...
     * @param applicationAuthServerConfigurer configurer
     * @throws Exception
     */
    default void configure(ApplicationAuthServerConfigurer<HttpSecurity> applicationAuthServerConfigurer) throws Exception {

    }
}
