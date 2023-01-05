package com.generatera.resource.server.config;

import com.generatera.resource.server.config.bootstrap.DefaultResourceServerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface LightningResourceServerConfigurer {
    void configure(DefaultResourceServerConfigurer<HttpSecurity> resourceServerConfigurer);
}
