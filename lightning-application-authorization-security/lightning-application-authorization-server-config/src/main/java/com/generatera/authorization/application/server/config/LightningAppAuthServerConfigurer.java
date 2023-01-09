package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface LightningAppAuthServerConfigurer {

    default void configure(HttpSecurity securityBuilder) throws Exception {

    }
}
