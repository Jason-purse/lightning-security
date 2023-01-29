package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.util.AuthConfigurerUtils;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.security.authorization.server.specification.components.authentication.LightningAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;

@Configuration
@AutoConfiguration
@RequiredArgsConstructor
public class OAuth2LoginAuthenticationEntryPointConfiguration {

    @Bean
    public LightningAuthServerConfigurer oauth2LoginAuthenticationEntryPointConfigurer() {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                OAuth2LoginConfigurer<HttpSecurity> httpSecurityOAuth2LoginConfigurer = securityBuilder.oauth2Login();
                LightningAuthenticationEntryPoint authenticationEntryPoint = AuthConfigurerUtils.getAuthenticationEntryPoint(securityBuilder);
                httpSecurityOAuth2LoginConfigurer
                        .successHandler(authenticationEntryPoint)
                        .failureHandler(authenticationEntryPoint);
            }
        };
    }
}
