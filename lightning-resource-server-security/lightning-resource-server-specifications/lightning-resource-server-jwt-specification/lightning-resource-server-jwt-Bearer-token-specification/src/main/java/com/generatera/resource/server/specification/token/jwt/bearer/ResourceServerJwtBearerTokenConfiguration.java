package com.generatera.resource.server.specification.token.jwt.bearer;

import com.generatera.resource.server.config.token.LightningTokenAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

/**
 * 自动装配一个 jwt bearer Token 认证过滤器 ..
 */
@Configuration
public class ResourceServerJwtBearerTokenConfiguration {

    @Bean
    @ConditionalOnMissingBean(LightningTokenAuthenticationFilter.class)
    public LightningBearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new LightningBearerTokenAuthenticationFilter(authenticationManager);
    }
}
