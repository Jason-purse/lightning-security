package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.authentication.DefaultLightningOAuth2LoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@AutoConfiguration
@RequiredArgsConstructor
public class OAuth2LoginAuthenticationEntryPointConfiguration {

    private final OAuth2LoginProperties oAuth2LoginProperties;

    private final LightningAuthenticationTokenService authenticationTokenService;

    @Bean
    @ConditionalOnMissingBean(LightningOAuth2LoginAuthenticationEntryPoint.class)
    public DefaultLightningOAuth2LoginAuthenticationEntryPoint entryPoint(
            LightningTokenGenerator<LightningToken> tokenGenerator
    ) {
        DefaultLightningOAuth2LoginAuthenticationEntryPoint point = new DefaultLightningOAuth2LoginAuthenticationEntryPoint();
        OAuth2LoginProperties.BackendSeparation backendSeparation = oAuth2LoginProperties.getBackendSeparation();
        if (StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
            point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
        }
        if (backendSeparation.getEnableAuthErrorDetail()) {
            point.setEnableAuthErrorDetails(Boolean.TRUE);
        }
        if (StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
            point.setAuthErrorMessage(backendSeparation.getLoginFailureMessage());
        }
        Assert.notNull(authenticationTokenService, "authenticationTokenService must not be null !!!");
        point.setAuthenticationTokenService(authenticationTokenService);
        // 必须存在
        point.setTokenGenerator(tokenGenerator);

        return point;
    }

    @Bean
    public LightningAuthServerConfigurer oauth2LoginAuthenticationEntryPointConfigurer(
            DefaultLightningOAuth2LoginAuthenticationEntryPoint authenticationEntryPoint
    ) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                OAuth2LoginConfigurer<HttpSecurity> httpSecurityOAuth2LoginConfigurer = securityBuilder.oauth2Login();
                httpSecurityOAuth2LoginConfigurer
                        .successHandler(authenticationEntryPoint)
                        .failureHandler(authenticationEntryPoint);
            }
        };
    }
}
