package com.generatera.oauth2.resource.server.test.jwt.bearer.token.config;

import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.bootstrap.DefaultResourceServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OAuth2ResourceServerConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security, LightningResourceServerConfigurer configurer) throws Exception {
        DefaultResourceServerConfigurer<HttpSecurity> serverConfigurer = new DefaultResourceServerConfigurer<>();
        security.apply(serverConfigurer);
        configurer.configure(serverConfigurer);
        return security.build();
    }
}
