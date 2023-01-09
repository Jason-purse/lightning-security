package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.form.login.config.authentication.LightningFormLoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.form.login.config.token.DefaultFormLoginTokenGenerator;
import com.generatera.authorization.application.server.form.login.config.token.FormLoginTokenGenerator;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JWKSourceProvider;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder;
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

    @Autowired
    private JWKSourceProvider jwkSourceProvider;

    @Autowired(required = false)
    private FormLoginTokenGenerator tokenGenerator;

    @Autowired(required = false)
    private LightningTokenGenerator<LightningToken> delegate;


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

        LightningTokenGenerator<LightningToken> tokenLightningTokenGenerator = Objects.requireNonNullElseGet(tokenGenerator,
                () -> Objects.requireNonNullElseGet(delegate,() ->
                    new DelegatingLightningTokenGenerator(
                            new DefaultLightningAccessTokenGenerator(),
                            new DefaultLightningRefreshTokenGenerator(),
                            new DefaultLightningJwtGenerator(new NimbusJwtEncoder(jwkSourceProvider.getJWKSource()))
                    )
                ));

        Assert.notNull(authenticationTokenService,"authenticationTokenService must not be null !!!");
        LightningFormLoginAuthenticationEntryPoint point = new LightningFormLoginAuthenticationEntryPoint(
                new DefaultFormLoginTokenGenerator(tokenLightningTokenGenerator), tokenSettingsProvider, authenticationTokenService);
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
