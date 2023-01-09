package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.config.authentication.RedirectAuthenticationSuccessOrFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 表单登陆配置
 */
@Configuration
@AutoConfiguration
@AutoConfigureAfter(ApplicationAuthServerConfig.class)
@EnableConfigurationProperties(FormLoginProperties.class)
@Import({FormLoginConfigurationImportSelector.class})
@RequiredArgsConstructor
public class ApplicationFormLoginConfiguration {
    private final FormLoginProperties formLoginProperties;

    @Bean
    public LightningAppAuthServerConfigurer lightningFormLoginConfigurer(
            @Autowired(required = false)
            AuthenticationSuccessHandler authenticationSuccessHandler,
            @Autowired(required = false)
            AuthenticationFailureHandler authenticationFailureHandler) {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity builder) throws Exception {
                FormLoginConfigurer<HttpSecurity> formLoginConfigurer = builder.formLogin();

                List<String> patterns = new LinkedList<>();
                // 如果是前后端分离的 ..
                if (Objects.requireNonNullElse(formLoginProperties.getIsSeparation(), Boolean.FALSE)) {
                    separationConfig(formLoginConfigurer, authenticationSuccessHandler, authenticationFailureHandler);
                } else {
                    // 前后端不分离配置 ..
                    FormLoginProperties.NoSeparation noSeparation = formLoginProperties.getNoSeparation();
                    if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                        formLoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                    }

                    if (noSeparation.getEnableSavedRequestForward() != null && noSeparation.getEnableSavedRequestForward()) {
                        if (StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
                            formLoginConfigurer.defaultSuccessUrl(noSeparation.getDefaultSuccessUrl());
                            patterns.add(noSeparation.getDefaultSuccessUrl());
                        }
                    } else {
                        noSeparationConfig(formLoginConfigurer, patterns, noSeparation);
                    }
                }

                genericConfig(formLoginConfigurer);
                permission(formLoginConfigurer, patterns);

            }
        };
    }

    private void genericConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer) {
        // username / password
        if (StringUtils.hasText(formLoginProperties.getUsernameParameterName())) {
            formLoginConfigurer.usernameParameter(formLoginProperties.getUsernameParameterName());
        }

        if (StringUtils.hasText(formLoginProperties.getPasswordParameterName())) {
            formLoginConfigurer.passwordParameter(formLoginProperties.getPasswordParameterName());
        }

        if (StringUtils.hasText(formLoginProperties.getLoginProcessUrl())) {
            formLoginConfigurer.loginProcessingUrl(formLoginProperties.getLoginProcessUrl());
        }
    }

    private static void permission(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, List<String> patterns) {
        // 资源放行
        try {
            formLoginConfigurer.and()
                    .authorizeHttpRequests()
                    .antMatchers(patterns.toArray(String[]::new))
                    .permitAll();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void noSeparationConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, List<String> patterns, FormLoginProperties.NoSeparation noSeparation) {
        // 针对于 转发url 是post请求,所以静态资源不支持,会报错 405
        // 所以需要自己重写 登陆成功的跳转地址 ..
        if (noSeparation.getEnableForward() != null && noSeparation.getEnableForward()) {
            if (StringUtils.hasText(noSeparation.getSuccessForwardOrRedirectUrl())) {
                formLoginConfigurer.successForwardUrl(noSeparation.getSuccessForwardOrRedirectUrl());
                patterns.add(noSeparation.getSuccessForwardOrRedirectUrl());
            }

            if (StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                formLoginConfigurer.failureForwardUrl(noSeparation.getFailureForwardOrRedirectUrl());
                patterns.add(noSeparation.getFailureForwardOrRedirectUrl());
            }
        } else {
            // 如果都没有填写转发地址 ..
            // 这里直接启用 强制重定向到 /
            if (StringUtils.hasText(noSeparation.getSuccessForwardOrRedirectUrl())) {
                formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getSuccessForwardOrRedirectUrl()));
                patterns.add(noSeparation.getSuccessForwardOrRedirectUrl());
            } else {
                if (!noSeparation.getEnableSavedRequestForward()) {
                    formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler("/"));
                    patterns.add("/");
                }
            }

            if (StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getFailureForwardOrRedirectUrl()));
                patterns.add(noSeparation.getFailureForwardOrRedirectUrl());
            } else {
                if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                    formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getLoginPageUrl()));
                } else {
                    formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler("/login"));
                }
            }
        }
    }

    private static void separationConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationSuccessHandler,"authenticationSuccessHandler must not be null !!!");
        Assert.notNull(authenticationFailureHandler,"authenticationFailureHandler must not be null !!!");
        // 前后端分离的 handler 配置 ..
        formLoginConfigurer.successHandler(authenticationSuccessHandler);
        formLoginConfigurer.failureHandler(authenticationFailureHandler);
    }
}
