package com.generatera.resource.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface LightningResourceServerConfigurer {
    public void configure(HttpSecurity security) throws Exception;
}
