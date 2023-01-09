package com.generatera.authorization.application.server.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 此配置作为 整个授权服务器的控制中心(模板配置)
 *
 * 当自定义 AuthExtSecurityConfigurer的情况下,枢纽控制将被破坏,请注意实现 ...
 * 除此之外还处理白名单访问请求路径 ...
 */
@Configuration
@AutoConfiguration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableConfigurationProperties(ApplicationAuthServerProperties.class)
public class ApplicationAuthServerConfig {

    @Bean
    @ConditionalOnMissingBean(AuthExtSecurityConfigurer.class)
    public AuthExtSecurityConfigurer oAuth2ExtSecurityConfigurer(List<LightningAppAuthServerConfigurer> configurers) {
        return new AuthExtSecurityConfigurer(configurers);
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity,
                                            AuthExtSecurityConfigurer configurer, ApplicationAuthServerProperties properties) throws Exception {
        return httpSecurity
                .apply(configurer)
                .and()
                .apply(permissionHandle(properties))
                .and()
                .build();
    }

    /**
     * 白名单放行
     */
    @NotNull
    private SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> permissionHandle(ApplicationAuthServerProperties properties) {
        return new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
            @Override
            public void init(HttpSecurity builder) throws Exception {

                // 最后添加这个
                AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                        authorizationManagerRequestMatcherRegistry = builder
                        .authorizeHttpRequests();
                if (!CollectionUtils.isEmpty(properties.getPermission().getUrlWhiteList())) {
                    authorizationManagerRequestMatcherRegistry
                            .mvcMatchers(
                                    properties.getPermission().getUrlWhiteList().toArray(String[]::new)
                            )
                            .permitAll();
                }

                authorizationManagerRequestMatcherRegistry
                        .anyRequest()
                        .authenticated()
                        .and()
                        .csrf()
                        .disable();
            }
        };
    }

}
