package com.generatera.authorization.application.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableConfigurationProperties(ApplicationAuthServerProperties.class)
public class ApplicationAuthServerConfig {

    @Bean
    public OAuth2ExtSecurityConfigurer oAuth2ExtSecurityConfigurer(ApplicationAuthServerProperties appAuthProperties,
                                                                   @Autowired(required = false) List<LightningAppAuthServerConfigurer> configurers) {
        return new OAuth2ExtSecurityConfigurer(appAuthProperties,configurers);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity,
                                            OAuth2ExtSecurityConfigurer configurer) throws Exception {
        return httpSecurity
                .apply(configurer)
                .and()
                .authorizeHttpRequests()
                .anyRequest()
                .authenticated()
                .and()
                .build();
    }
}
