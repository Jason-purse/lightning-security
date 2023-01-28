package com.generatera.authorization.server.common.configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface LightningAuthServerConfigurer {

    default void configure(HttpSecurity securityBuilder) throws Exception {

    }
}
