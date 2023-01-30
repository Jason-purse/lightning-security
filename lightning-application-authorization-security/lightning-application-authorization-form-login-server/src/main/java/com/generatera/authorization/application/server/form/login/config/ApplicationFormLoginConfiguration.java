package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.AppAuthConfigConstant;
import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.LightningAppAuthServerBootstrapConfigurer;
import com.generatera.authorization.application.server.config.authentication.RedirectAuthenticationSuccessOrFailureHandler;
import com.generatera.authorization.application.server.form.login.config.components.UserDetailsServiceAutoConfiguration;
import com.generatera.authorization.application.server.form.login.config.util.FormLoginUtils;
import com.generatera.authorization.server.common.configuration.util.LogUtil;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.generatera.authorization.application.server.config.util.StringUtils.normalize;

/**
 * 表单登陆配置
 */
@Configuration
@AutoConfiguration
@AutoConfigureAfter(ApplicationAuthServerConfig.class)
@EnableConfigurationProperties(FormLoginProperties.class)
@Import({FormLoginConfigurationImportSelector.class, UserDetailsServiceAutoConfiguration.class})
@RequiredArgsConstructor
public class ApplicationFormLoginConfiguration {

    private final FormLoginProperties formLoginProperties;
    private final ApplicationAuthServerProperties authServerProperties;

    /**
     * 表单登录配置 ...
     */
    @Bean
    public LightningAppAuthServerBootstrapConfigurer formLoginConfigurer() {
        return new LightningAppAuthServerBootstrapConfigurer() {

            @Override
            public void configure(HttpSecurity security) throws Exception {
                security.setSharedObject(FormLoginProperties.class,formLoginProperties);

                FormLoginProperties.NoSeparation noSeparation = formLoginProperties.getNoSeparation();
                String appAuthPrefix = ElvisUtil.stringElvis(normalize(authServerProperties.getAppAuthPrefix()), AppAuthConfigConstant.APP_AUTH_SERVER_PREFIX);

                FormLoginConfigurer<HttpSecurity> formLoginConfigurer = security.formLogin();

                Collection<String> patterns = new LinkedHashSet<>();

                // 如果不是前后端分离的 ..
                if (!Objects.requireNonNullElse(authServerProperties.getIsSeparation(), Boolean.FALSE)) {
                    noSeparationOtherConfig(formLoginConfigurer, patterns, noSeparation, appAuthPrefix);
                }

                genericConfig(formLoginConfigurer, appAuthPrefix);

                LogUtil.prettyLog("form login authorization server enabled .....");
            }
        };
    }


    private static void noSeparationOtherConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer,
                                                Collection<String> patterns, FormLoginProperties.NoSeparation noSeparation,
                                                String authServerPrefix) throws Exception {


        ApplicationAuthServerProperties serverProperties = formLoginConfigurer.and().getSharedObject(ApplicationAuthServerProperties.class);
        authResponse(formLoginConfigurer, patterns,serverProperties.getNoSeparation() , authServerPrefix);

        ApplicationAuthServerProperties authServerProperties = formLoginConfigurer.and().getSharedObject(ApplicationAuthServerProperties.class);

        logoutWithLogin(formLoginConfigurer, noSeparation, authServerPrefix, authServerProperties);


        formLoginConfigurer.and()
                .authorizeHttpRequests()
                .antMatchers(patterns.toArray(String[]::new))
                .permitAll();
    }

    public static void logoutWithLogin(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, FormLoginProperties.NoSeparation noSeparation, String authServerPrefix, ApplicationAuthServerProperties authServerProperties) throws Exception {
        // 表示登出页面处理  ...
        ApplicationAuthServerProperties.NoSeparation mainNoSeparation = authServerProperties.getNoSeparation();
        String loginPageUrl = authServerPrefix + normalize(ElvisUtil.stringElvis(noSeparation.getLoginPageUrl(),mainNoSeparation.getLoginPageUrl()));

        // 自定义的登录页面
        if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
            formLoginConfigurer.loginPage(loginPageUrl);

            // 这就是支持默认登录页面的行为 ...
            // 然后就是 认证端点 处理 ...
            ExceptionHandlingConfigurer<HttpSecurity> exceptionHandling = formLoginConfigurer.and()
                    .exceptionHandling();

            // 可以重新配置 ...
            exceptionHandling.authenticationEntryPoint(
                    (request, response, authException) -> response.sendRedirect(loginPageUrl)
            );
        }
        else {
            // 默认登录页面配置
            formLoginConfigurer
                    .and()
                    .apply(
                            new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                                @Override
                                public void init(HttpSecurity builder) throws Exception {
                                    // 这个不存在就填充 ..
                                    String logoutSuccessUrl  = authServerPrefix + ElvisUtil.stringElvis(normalize(authServerProperties.getNoSeparation().getLogoutSuccessUrl()),"/login?logout");
                                    FormLoginUtils.configDefaultLoginPageGeneratorFilter(builder, loginPageUrl,logoutSuccessUrl);
                                    // 晚一点配置 ..
                                    formLoginConfigurer.loginPage(loginPageUrl);
                                }
                            });
        }
    }

    private static void authResponse(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, Collection<String> patterns, ApplicationAuthServerProperties.NoSeparation noSeparation, String authServerPrefix) {
        // 针对于 转发url 是post请求,所以静态资源不支持,会报错 405
        // 所以需要自己重写 登陆成功的跳转地址 ..
        if (noSeparation.getEnableForward() != null && noSeparation.getEnableForward()) {

            if (StringUtils.hasText(noSeparation.getSuccessForwardOrRedirectUrl())) {
                String path = authServerPrefix + normalize(noSeparation.getSuccessForwardOrRedirectUrl());
                formLoginConfigurer.successForwardUrl(path);
                patterns.add(path);
            }

            if (StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                String path = authServerPrefix + normalize(noSeparation.getFailureForwardOrRedirectUrl());
                formLoginConfigurer.failureForwardUrl(path);
                patterns.add(path);
            }
        } else {
            // 如果都没有填写转发地址 ..
            // 这里直接启用 强制重定向到 /
            if (StringUtils.hasText(noSeparation.getSuccessForwardOrRedirectUrl())) {
                String path = authServerPrefix + normalize(noSeparation.getSuccessForwardOrRedirectUrl());
                formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler(path));
                patterns.add(path);
            } else {
                if (!noSeparation.getEnableSavedRequestForward()) {
                    formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler("/"));
                    patterns.add("/");
                }
            }

            if (StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                String path = normalize(noSeparation.getFailureForwardOrRedirectUrl());
                formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(path));
                patterns.add(path);
            } else {
                if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                    String path = authServerPrefix + normalize(noSeparation.getLoginPageUrl());
                    formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(path));
                    patterns.add(path);
                } else {
                    String path = authServerPrefix + "/login?error";
                    patterns.add(path);
                    formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(path));
                }
            }
        }

        // 自动设置 ...
        if(StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
            patterns.add(noSeparation.getDefaultSuccessUrl());
            formLoginConfigurer.defaultSuccessUrl(noSeparation.getDefaultSuccessUrl());
        }
    }


    private void genericConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, String authServerPrefix) throws Exception {

        // username / password
        if (StringUtils.hasText(formLoginProperties.getUsernameParameterName())) {
            formLoginConfigurer.usernameParameter(formLoginProperties.getUsernameParameterName());
        }

        if (StringUtils.hasText(formLoginProperties.getPasswordParameterName())) {
            formLoginConfigurer.passwordParameter(formLoginProperties.getPasswordParameterName());
        }


        if (StringUtils.hasText(formLoginProperties.getLoginProcessUrl())) {
            String path = authServerPrefix + normalize(formLoginProperties.getLoginProcessUrl());
            formLoginConfigurer.loginProcessingUrl(path);
        }

    }


}
