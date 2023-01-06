package com.generatera.resource.server.specification.token.jwt.bearer;

import com.generatera.resource.server.config.LightningResourceServerOtherConfigurer;
import com.generatera.resource.server.config.token.LightningSecurityContextRepository;
import com.generatera.resource.server.config.token.LightningTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 自动装配一个 jwt bearer Token 认证过滤器 ..
 *
 * 非 oauth2时自动装配 ..
 */
@Configuration
public class ResourceServerJwtBearerTokenConfiguration {

    @Bean
    @ConditionalOnMissingBean(LightningTokenAuthenticationFilter.class)
    public LightningBearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(
            AuthenticationManager authenticationManager,
            @Autowired(required = false)
            LightningBearerTokenResolver bearerTokenResolver,
            @Autowired(required = false)
            LightningBearerTokenAuthenticationEntryPoint authenticationEntryPoint,
            @Autowired(required = false)
            LightningBearerTokenAuthenticationFailureHandler authenticationFailureHandler,
            @Autowired(required = false)
            LightningSecurityContextRepository securityContextRepository
            ) {
        LightningBearerTokenAuthenticationFilter authenticationFilter = new LightningBearerTokenAuthenticationFilter(authenticationManager);
        if (bearerTokenResolver != null) {
            authenticationFilter.setBearerTokenResolver(bearerTokenResolver);
        }
        if (authenticationEntryPoint != null) {
            authenticationFilter.setAuthenticationEntryPoint(authenticationEntryPoint);
        }

        if (authenticationFailureHandler != null) {
            authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        }
        if (securityContextRepository != null) {
            authenticationFilter.setSecurityContextRepository(securityContextRepository);
        }

        return authenticationFilter;
    }
}
