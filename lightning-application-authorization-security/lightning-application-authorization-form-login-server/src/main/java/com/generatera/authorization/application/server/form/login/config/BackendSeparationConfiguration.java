package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.server.common.configuration.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.config.securityContext.DefaultSecurityContextRepository;
import com.generatera.authorization.application.server.form.login.config.authentication.DefaultLightningFormLoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.form.login.config.authentication.LightningFormLoginAuthenticationEntryPoint;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.authentication.LightningSecurityContextRepository;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@AutoConfiguration
@RequiredArgsConstructor
public class BackendSeparationConfiguration {

    private final LightningAuthenticationTokenService authenticationTokenService;

    private final TokenSettingsProvider tokenSettingsProvider;

    private final FormLoginProperties formLoginProperties;

    @Bean
    @ConditionalOnMissingBean(LightningSecurityContextRepository.class)
    public LightningSecurityContextRepository securityContextRepository() {
        return new DefaultSecurityContextRepository();
    }


    @Bean
    public LightningAppAuthServerConfigurer FormLoginAuthenticationEntryPointConfigurer(
            DefaultLightningFormLoginAuthenticationEntryPoint formLoginAuthenticationEntryPoint,
            LightningSecurityContextRepository securityContextRepository
    ) {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                FormLoginConfigurer<HttpSecurity> httpSecurityFormLoginConfigurer = securityBuilder.formLogin();
                httpSecurityFormLoginConfigurer
                        .successHandler(formLoginAuthenticationEntryPoint)
                        .failureHandler(formLoginAuthenticationEntryPoint)
                        .and()
                        .sessionManagement()
                        // 无状态session 会话
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                        // 也就是 securityContext 通过 resource server 进行加载
                        .securityContext()
                        .securityContextRepository(securityContextRepository);
            }
        };
    }

    /**
     * 代理此方法 ..
     *
     * @return authentication success / failure 都是同一个对象
     */
    @Bean
    @ConditionalOnMissingBean(LightningFormLoginAuthenticationEntryPoint.class)
    public DefaultLightningFormLoginAuthenticationEntryPoint lightningFormLoginAuthenticationEntryPoint(
            LightningTokenGenerator<LightningToken> tokenGenerator
    ) {
        Assert.notNull(authenticationTokenService, "authenticationTokenService must not be null !!!");
        DefaultLightningFormLoginAuthenticationEntryPoint point = new DefaultLightningFormLoginAuthenticationEntryPoint(
                tokenGenerator, tokenSettingsProvider, authenticationTokenService);
        FormLoginProperties.BackendSeparation backendSeparation = formLoginProperties.getBackendSeparation();

        if (StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
            point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
        }

        point.setEnableAccountStatusInform(backendSeparation.getEnableAccountStatusInform());

        if (ObjectUtils.isNotEmpty(backendSeparation.getEnableAccountStatusInform()) && backendSeparation.getEnableAccountStatusInform()) {
            if (StringUtils.hasText(backendSeparation.getAccountLockedMessage())) {
                point.setAccountStatusLockedMessage(backendSeparation.getAccountLockedMessage());
            }
            if (StringUtils.hasText(backendSeparation.getAccountExpiredMessage())) {
                point.setAccountStatusExpiredMessage(backendSeparation.getAccountExpiredMessage());
            }
        }

        if (StringUtils.hasText(backendSeparation.getAccountStatusMessage())) {
            point.setAccountStatusMessage(backendSeparation.getAccountStatusMessage());
        }

        if (StringUtils.hasText(backendSeparation.getBadCredentialMessage())) {
            point.setBadCredentialsMessage(backendSeparation.getBadCredentialMessage());
        }

        if (StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
            point.setLoginFailureMessage(backendSeparation.getLoginFailureMessage());
        }

        return point;
    }
}
