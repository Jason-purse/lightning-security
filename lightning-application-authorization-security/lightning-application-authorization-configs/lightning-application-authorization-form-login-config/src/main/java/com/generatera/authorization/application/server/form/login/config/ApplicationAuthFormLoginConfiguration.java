package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.LightningFormLoginConfigurer;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

/**
 * 表单登陆配置
 */
@Configuration
@EnableConfigurationProperties(FormLoginProperties.class)
@ConditionalOnProperty(prefix = "lightning.auth.app.server.config.form-login",name = "enable",havingValue = "true")
public class ApplicationAuthFormLoginConfiguration  {

    @Autowired
    private FormLoginProperties formLoginProperties;

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
     * @return authentication success / failure 都是同一个对象
     */
    private LightningFormLoginAuthenticationEntryPoint lightningFormLoginAuthenticationEntryPoint() {

        LightningFormLoginAuthenticationEntryPoint point = new LightningFormLoginAuthenticationEntryPoint();
        FormLoginProperties.BackendSeparation backendSeparation = formLoginProperties.getBackendSeparation();

        if(StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
            point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
        }

        point.setEnableAccountStatusInform(backendSeparation.getEnableAccountStatusInform());

        if(ObjectUtils.isNotEmpty(backendSeparation.getEnableAccountStatusInform()) && backendSeparation.getEnableAccountStatusInform()) {
            if(StringUtils.hasText(backendSeparation.getAccountLockedMessage())) {
                point.setAccountStatusLockedMessage(backendSeparation.getAccountLockedMessage());
            }
            if(StringUtils.hasText(backendSeparation.getAccountExpiredMessage())) {
                point.setAccountStatusExpiredMessage(backendSeparation.getAccountExpiredMessage());
            }
        }

        if(StringUtils.hasText(backendSeparation.getAccountStatusMessage())) {
            point.setAccountStatusMessage(backendSeparation.getAccountStatusMessage());
        }

        if(StringUtils.hasText(backendSeparation.getBadCredentialMessage())) {
            point.setBadCredentialsMessage(backendSeparation.getBadCredentialMessage());
        }

        return point;
    }


    @Bean
    public LightningFormLoginConfigurer lightningFormLoginConfigurer(
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler) {
        return new LightningFormLoginConfigurer() {
            @Override
            public void configure(FormLoginConfigurer<HttpSecurity> formLoginConfigurer) {
                // 如果是前后端分离的 ..
                if (formLoginProperties.getIsSeparation()) {
                    FormLoginProperties.BackendSeparation backendSeparation = formLoginProperties.getBackendSeparation();

                    if (StringUtils.hasText(backendSeparation.getLoginRequestUrl())) {
                        formLoginConfigurer.loginProcessingUrl(backendSeparation.getLoginRequestUrl());
                    }

                    // 前后端分离的 handler 配置 ..
                    formLoginConfigurer.successHandler(authenticationSuccessHandler);
                    formLoginConfigurer.failureHandler(authenticationFailureHandler);
                }
                else {
                    // 前后端不分离配置 ..
                    FormLoginProperties.NoSeparation noSeparation = formLoginProperties.getNoSeparation();
                    if(StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                        formLoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                    }

                    if(StringUtils.hasText(noSeparation.getLoginProcessUrl())) {
                        formLoginConfigurer.loginProcessingUrl(noSeparation.getLoginProcessUrl());
                    }

                    if(StringUtils.hasText(noSeparation.getSuccessForwardUrl())) {
                        formLoginConfigurer.successForwardUrl(noSeparation.getSuccessForwardUrl());
                    }

                    if(StringUtils.hasText(noSeparation.getFailureForwardUrl())) {
                        formLoginConfigurer.failureForwardUrl(noSeparation.getSuccessForwardUrl());
                    }

                    // todo
                    //  不应该开启session
                    // 所以 需要重写(但是目前使用了  successForwardUrl,不需要处理了)
                    // default success url 等价于 successForwardUrl 所以我们使用一个即可 ..

                    // default success url 会导致使用之前的请求url进行 重定向 ..
                    // 我们是否应该这样做 ..
                    // 目前只要登陆,直接跳转到指定的url 即可,所以还是使用 successForwardUrl
                    // 后续如果需要跳转到目标url 可以添加配置 ..
                }

                // username / password
                if(StringUtils.hasText(formLoginProperties.getUsernameParameterName())) {
                    formLoginConfigurer.usernameParameter(formLoginProperties.getUsernameParameterName());
                }

                if(StringUtils.hasText(formLoginProperties.getPasswordParameterName())) {
                    formLoginConfigurer.passwordParameter(formLoginProperties.getPasswordParameterName());
                }

            }
        };
    }
}
