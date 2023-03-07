package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.LightningAppAuthServerBootstrapConfigurer;
import com.generatera.authorization.application.server.config.authentication.RedirectAuthenticationSuccessOrFailureHandler;
import com.generatera.authorization.application.server.config.token.LightningDaoAuthenticationProvider;
import com.generatera.authorization.application.server.config.token.LightningUserDetailsProvider;
import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.generatera.authorization.application.server.form.login.config.components.*;
import com.generatera.authorization.application.server.form.login.config.util.FormLoginUtils;
import com.generatera.security.authorization.server.specification.util.LogUtil;
import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

import static com.generatera.authorization.application.server.config.ApplicationAuthServerProperties.NoSeparation.DEFAULT_FAILURE_FORWARD_OR_REDIRECT_URL;
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


    @Bean
    @Qualifier("userAuthenticationProvider")
    @Primary
    public DaoAuthenticationProvider daoAuthenticationProvider(
            LightningUserDetailService userDetailsService,
            @Autowired(required = false)
                    PasswordEncoder passwordEncoder,
            @Autowired(required = false)
                    UserDetailsPasswordService passwordManager
    ) {
        LightningUserDetailService finalUserDetailsService = userDetailsService;
        userDetailsService = new LightningUserDetailService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                UserDetails userDetails = finalUserDetailsService.loadUserByUsername(username);
                if (!LightningUserPrincipal.class.isAssignableFrom(userDetails.getClass())) {
                    return new DefaultLightningUserDetails(userDetails);
                }
                return userDetails;
            }

            @Override
            public LightningUserPrincipal mapAuthenticatedUser(LightningUserPrincipal userPrincipal) {
                return finalUserDetailsService.mapAuthenticatedUser(userPrincipal);
            }
        };

        OptmizedDaoAuthenticationProvider provider = new OptmizedDaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        if (passwordEncoder != null) {
            provider.setPasswordEncoder(passwordEncoder);
        }

        if (passwordManager != null) {
            provider.setUserDetailsPasswordService(passwordManager);
        }
        return provider;
    }


    @Bean
    public LightningUserDetailsProvider formUserDetailsProvider(UserDetailsService userDetailsService) {
        return new FormLoginUserDetailsProvider(userDetailsService);
    }

    @Bean
    public LightningDaoAuthenticationProvider formLoginDaoAuthenticationProvider(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new FormDaoAuthenticationProvider(daoAuthenticationProvider);
    }

    /**
     * 表单登录配置 ...
     */
    @Bean
    public LightningAppAuthServerBootstrapConfigurer formLoginConfigurer() {
        return new LightningAppAuthServerBootstrapConfigurer() {

            @Override
            public void configure(HttpSecurity security) throws Exception {
                security.setSharedObject(FormLoginProperties.class, formLoginProperties);

                FormLoginProperties.NoSeparation noSeparation = formLoginProperties.getNoSeparation();

                FormLoginConfigurer<HttpSecurity> formLoginConfigurer = security.formLogin();

                Collection<String> patterns = new LinkedHashSet<>();

                ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(security);
                // 如果不是前后端分离的 ..
                if (!authServerProperties.isSeparation()) {
                    noSeparationOtherConfig(formLoginConfigurer, patterns, noSeparation, applicationAuthServerUtils);
                }

                genericConfig(formLoginConfigurer, applicationAuthServerUtils);

                LogUtil.prettyLog("form login authorization server enabled .....");
            }
        };
    }


    private static void noSeparationOtherConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer,
                                                Collection<String> patterns, FormLoginProperties.NoSeparation noSeparation,
                                                ApplicationAuthServerUtils applicationAuthServerUtils) throws Exception {


        authResponse(formLoginConfigurer, patterns, applicationAuthServerUtils);


        logoutWithLogin(formLoginConfigurer, noSeparation,applicationAuthServerUtils);


        formLoginConfigurer.and()
                .authorizeHttpRequests()
                .antMatchers(patterns.toArray(String[]::new))
                .permitAll();
    }

    public static void logoutWithLogin(FormLoginConfigurer<HttpSecurity> formLoginConfigurer,
                                       FormLoginProperties.NoSeparation noSeparation,
                                       ApplicationAuthServerUtils applicationAuthServerUtils) throws Exception {

        // 表示登出页面处理  ...
        ApplicationAuthServerProperties.NoSeparation mainNoSeparation = applicationAuthServerUtils.getProperties().getNoSeparation();
        String loginPageUrl = normalize(ElvisUtil.stringElvis(noSeparation.getLoginPageUrl(), mainNoSeparation.getLoginPageUrl()));
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
        } else {
            // 默认登录页面配置
            formLoginConfigurer
                    .and()
                    .apply(
                            new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                                @Override
                                public void init(HttpSecurity builder) throws Exception {
                                    FormLoginUtils.configDefaultLoginPageGeneratorFilter(builder, loginPageUrl);
                                    // 晚一点配置 ..(因为 逃避 此配置器的 customizePage 检测)
                                    formLoginConfigurer.loginPage(loginPageUrl);
                                }
                            });
        }
    }

    private static void authResponse(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, Collection<String> patterns, ApplicationAuthServerUtils applicationAuthServerUtils) {
        // 针对于 转发url 是post请求,所以静态资源不支持,会报错 405
        // 所以需要自己重写 登陆成功的跳转地址 ..
        ApplicationAuthServerProperties properties = applicationAuthServerUtils.getProperties();
        ApplicationAuthServerProperties.NoSeparation noSeparation = properties.getNoSeparation();
        if (noSeparation.isEnableForward()) {

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
                if (!noSeparation.isEnableSavedRequestForward()) {
                    formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler("/"));
                    patterns.add("/");
                }
            }

            if (StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(
                        noSeparation.getFailureForwardOrRedirectUrl()
                ));
                patterns.add(noSeparation.getFailureForwardOrRedirectUrl());
            } else {
                if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                    formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(
                            noSeparation.getLoginPageUrl()
                    ));
                    patterns.add(noSeparation.getLoginPageUrl());
                } else {
                    String path = DEFAULT_FAILURE_FORWARD_OR_REDIRECT_URL;
                    patterns.add(path);
                    formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(path));
                }
            }
        }

        // 自动设置 ...
        if (StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
            patterns.add(noSeparation.getDefaultSuccessUrl());
            formLoginConfigurer.defaultSuccessUrl(noSeparation.getDefaultSuccessUrl());
        }
    }


    private void genericConfig(FormLoginConfigurer<HttpSecurity> formLoginConfigurer, ApplicationAuthServerUtils applicationAuthServerUtils) throws Exception {

        // username / password
        if (StringUtils.hasText(formLoginProperties.getUsernameParameterName())) {
            formLoginConfigurer.usernameParameter(formLoginProperties.getUsernameParameterName());
        }

        if (StringUtils.hasText(formLoginProperties.getPasswordParameterName())) {
            formLoginConfigurer.passwordParameter(formLoginProperties.getPasswordParameterName());
        }


        if (StringUtils.hasText(formLoginProperties.getLoginProcessUrl())) {
            String path = normalize(formLoginProperties.getLoginProcessUrl());
            formLoginConfigurer.loginProcessingUrl(path);
        }

    }


}
