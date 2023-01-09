package com.generatera.resource.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

public interface LightningResourceServerConfigurer {
    public void configure(OAuth2ResourceServerConfigurer<HttpSecurity> configurer) throws Exception;
}
