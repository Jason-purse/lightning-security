package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 此配置作为 整个授权服务器的控制中心(模板配置)
 * <p>
 * 当自定义 AuthExtSecurityConfigurer的情况下,枢纽控制将被破坏,请注意实现 ...
 * 除此之外还处理白名单访问请求路径 ...
 */
@Configuration
@AutoConfiguration
@AutoConfigureAfter(AuthorizationServerCommonComponentsConfiguration.class)
@EnableConfigurationProperties(ApplicationAuthServerProperties.class)
@Import(ApplicationServerImportSelector.class)
@RequiredArgsConstructor
public class ApplicationAuthServerConfig {

    private final ApplicationAuthServerProperties properties;


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public LightningAuthServerConfigurer bootstrapAppAuthServer(
            @Autowired(required = false) List<LightningAppAuthServerBootstrapConfigurer> appAuthServerConfigurers
    ) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {

                // 分离场景下,才需要增加token 端点进行token 颁发 / 刷新和 撤销 ...
                // 不分离,直接记住我就行 ..(remember me 服务) ...
                // // TODO: 2023/1/30  可以基于 rememberme 标识 来提供刷新token
                ApplicationAuthServerProperties serverProperties = security.getSharedObject(ApplicationAuthServerProperties.class);
                if (serverProperties == null) {
                    security.setSharedObject(ApplicationAuthServerProperties.class, properties);
                }

                // 应用 授权服务器 ..utils initilize ..
                ApplicationAuthServerUtils.getApplicationAuthServerProperties(security);


                // 配置 普通应用级别的 授权服务器 ..
                if (appAuthServerConfigurers != null && !appAuthServerConfigurers.isEmpty()) {
                    for (LightningAppAuthServerBootstrapConfigurer appAuthServerConfigurer : appAuthServerConfigurers) {
                        appAuthServerConfigurer.configure(security);
                    }
                }
            }
        };
    }


    /**
     * 非分离下的 登录或者登出页面配置 ...
     */
    @Bean
    public LightningAppAuthServerBootstrapConfigurer loginOrLogoutPageConfig() {
        return new LightningAppAuthServerBootstrapConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {


                boolean isSeparation = properties.isSeparation();
                ApplicationAuthServerUtils applicationAuthServerPropertiesUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(securityBuilder);

                if (isSeparation) {
                    // 禁用csrf
                    // 要让应用能够正常退出 ..(无需csrf token 校验)
                    securityBuilder
                            .csrf()
                            .disable();
                }

                ApplicationAuthServerProperties.NoSeparation noSeparation = applicationAuthServerPropertiesUtils.getProperties().getNoSeparation();

                if (!isSeparation) {
                    String logoutPageUrl = noSeparation.getLogoutPageUrl();
                    LogoutConfigurer<HttpSecurity> logout = securityBuilder.logout();
                    // 先设置登出 url
                    logout.logoutUrl(logoutPageUrl);
                    if (StringUtils.isNotBlank(noSeparation.getLogoutProcessUrl())) {
                        // 设置logout process url
                        String logoutProcessUrl = noSeparation.getLogoutProcessUrl();
                        logout
                                .logoutRequestMatcher(
                                        new AntPathRequestMatcher(logoutProcessUrl, "POST"));
                    }
                    // 登出成功的url
                    logout.logoutSuccessUrl(noSeparation.getLogoutSuccessUrl());

                    // 配置 logout pageGeneratorFilter(由于默认的 限制太深) ...
                    AppAuthConfigurerUtils.configDefaultLogoutPageGeneratingFilter(securityBuilder);

                    // 放行登出页面
                    securityBuilder.csrf()
                            .ignoringAntMatchers(logoutPageUrl);

                    String loginPageUrl = noSeparation.getLoginPageUrl();
                    // 可以重新配置 ...
                    securityBuilder
                            .exceptionHandling()
                            .authenticationEntryPoint(
                                    (request, response, authException) -> response.sendRedirect(loginPageUrl)
                            );
                } else {
                    // 登出的成功消息处理
                    securityBuilder.logout()
                            .logoutRequestMatcher(new AntPathRequestMatcher(applicationAuthServerPropertiesUtils.getProperties().getBackendSeparation().getLogoutProcessUrl(),"POST"))
                            .logoutSuccessHandler(new LogoutSuccessHandler() {
                                @Override
                                public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                    AuthHttpResponseUtil.commence(response,
                                            JsonUtil.getDefaultJsonUtil()
                                                    .asJSON(
                                                            Result.success(
                                                                    properties.getBackendSeparation()
                                                                            .getLogoutSuccessMessage()
                                                            ))
                                    );
                                }
                            });
                }

                // 增加退出处理器
                securityBuilder.logout()
                        .addLogoutHandler(AppAuthConfigurerUtils.logoutHandler(securityBuilder));

            }
        };
    }


    /**
     * 尝试修改 session id name ...
     * 如果前后端分离,则不做任何事情 ...
     *
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> sessionIdentifierConfig() {
        return new WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>() {
            @Override
            public void customize(ConfigurableServletWebServerFactory factory) {
                factory.addInitializers(new ServletContextInitializer() {
                    @Override
                    public void onStartup(ServletContext servletContext) throws ServletException {
                        if (!properties.isSeparation()) {
                            factory.setSession(new Session());
                            // 引导 ...
                            ApplicationAuthServerUtils.bootstrap(properties);
                            ApplicationAuthServerUtils instance = ApplicationAuthServerUtils.getInstance();
                            if (org.springframework.util.StringUtils.hasText(instance.getProperties().getNoSeparation().getTokenIdentifier())) {
                                servletContext.getSessionCookieConfig().setName(instance.getProperties().getNoSeparation().getTokenIdentifier());
                            }
                        }
                    }
                });

            }
        };
    }

    /**
     * 引导token 相关端点的配置处理  ...
     */
    @Bean
    public LightningAppAuthServerBootstrapConfigurer tokenEndpointsConfig(
            @Autowired(required = false)
            List<LightningAppAuthServerConfigurer> configurers
    ) throws Exception {
        return new LightningAppAuthServerBootstrapConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                ApplicationAuthServerConfigurer<HttpSecurity> authServerConfigurer = new ApplicationAuthServerConfigurer<>();
                securityBuilder.apply(authServerConfigurer);
                // 设置为共享对象 ..
                securityBuilder.setSharedObject(ApplicationAuthServerConfigurer.class, authServerConfigurer);

                if (!CollectionUtils.isEmpty(configurers)) {
                    for (LightningAppAuthServerConfigurer configurer : configurers) {
                        configurer.configure(authServerConfigurer);
                    }
                }

                // 配置阶段放行 ..
                securityBuilder.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                    @Override
                    public void init(HttpSecurity builder) throws Exception {
                        // pass,仅仅只是提供这个配置器
                        // 应用还可以提供此类LightningAppAuthServerConfigurer 进行进一步配置 ...
                        // 放行端点uri
                        securityBuilder
                                .authorizeHttpRequests()
                                .requestMatchers(authServerConfigurer.getEndpointsMatcher())
                                .permitAll();
                    }
                });

            }
        };
    }



}
