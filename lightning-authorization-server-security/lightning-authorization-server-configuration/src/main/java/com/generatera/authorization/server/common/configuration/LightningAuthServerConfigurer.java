package com.generatera.authorization.server.common.configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 授权服务器配置 ...
 */
public interface LightningAuthServerConfigurer {

    default void configure(HttpSecurity securityBuilder) throws Exception {

    }
}
