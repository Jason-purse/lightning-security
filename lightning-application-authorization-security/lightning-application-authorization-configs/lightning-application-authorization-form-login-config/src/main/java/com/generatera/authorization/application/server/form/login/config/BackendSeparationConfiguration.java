package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.specification.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.form.login.config.authentication.LightningFormLoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.form.login.config.token.DefaultFormLoginAuthenticationTokenGenerator;
import com.generatera.authorization.application.server.form.login.config.token.FormLoginAuthenticationTokenGenerator;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Objects;

@RequiredArgsConstructor
public class BackendSeparationConfiguration {

    private final LightningAuthenticationTokenService authenticationTokenService;

    private final TokenSettingsProvider tokenSettingsProvider;

    private final JWKSource<SecurityContext> jwkSource;

    @Autowired(required = false)
    private FormLoginAuthenticationTokenGenerator tokenGenerator;

    private final FormLoginProperties formLoginProperties;


    @Bean
    @ConditionalOnMissingBean({AuthenticationSuccessHandler.class})
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return lightningFormLoginAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean({AuthenticationFailureHandler.class})
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return lightningFormLoginAuthenticationEntryPoint();
    }


    /**
     * 代理此方法 ..
     *
     * @return authentication success / failure 都是同一个对象
     */
    private LightningFormLoginAuthenticationEntryPoint lightningFormLoginAuthenticationEntryPoint() {

        FormLoginAuthenticationTokenGenerator formLoginAuthenticationTokenGenerator = Objects.requireNonNullElseGet(tokenGenerator,
                () -> new DefaultFormLoginAuthenticationTokenGenerator(jwkSource));

        Assert.notNull(authenticationTokenService,"authenticationTokenService must not be null !!!");
        LightningFormLoginAuthenticationEntryPoint point = new LightningFormLoginAuthenticationEntryPoint(
                formLoginAuthenticationTokenGenerator, tokenSettingsProvider, authenticationTokenService);
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
