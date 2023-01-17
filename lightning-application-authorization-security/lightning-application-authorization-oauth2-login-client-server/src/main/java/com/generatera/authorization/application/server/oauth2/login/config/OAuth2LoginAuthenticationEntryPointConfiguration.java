package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.server.common.configuration.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint;
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
    public LightningOAuth2LoginAuthenticationEntryPoint entryPoint(
            LightningTokenGenerator<LightningToken> tokenGenerator
    ) {
        LightningOAuth2LoginAuthenticationEntryPoint point = new LightningOAuth2LoginAuthenticationEntryPoint();
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
    public LightningAppAuthServerConfigurer oauth2LoginAuthenticationEntryPointConfigurer(
            LightningOAuth2LoginAuthenticationEntryPoint authenticationEntryPoint
    ) {
        return new LightningAppAuthServerConfigurer() {
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
