package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;

public interface LightningAppAuthServerConfigurer {

    default void configure(FormLoginConfigurer<HttpSecurity> formLoginConfigurer) {

    }

    default void configure(OAuth2LoginConfigurer<HttpSecurity> formLoginConfigurer) {

    }

    default void configure(SecurityConfigurerAdapter<DefaultSecurityFilterChain,HttpSecurity> authorizationServerConfigurer) {

    }
}
