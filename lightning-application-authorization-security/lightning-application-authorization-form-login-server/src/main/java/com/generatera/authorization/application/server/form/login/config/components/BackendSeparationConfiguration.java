package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfigurer;
import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.config.securityContext.DefaultSecurityContextRepository;
import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.generatera.authorization.application.server.form.login.config.ApplicationFormLoginConfiguration;
import com.generatera.authorization.application.server.form.login.config.FormLoginProperties;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.security.authorization.server.specification.components.authentication.LightningAuthenticationEntryPoint;
import com.generatera.security.authorization.server.specification.components.authentication.LightningSecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

@AutoConfiguration
@RequiredArgsConstructor
public class BackendSeparationConfiguration {

    private final FormLoginProperties formLoginProperties;

    private final ApplicationAuthServerProperties authServerProperties;

    @Bean
    @ConditionalOnMissingBean(LightningSecurityContextRepository.class)
    public LightningSecurityContextRepository securityContextRepository() {
        return new DefaultSecurityContextRepository();
    }

    /**
     * 增加 FormLoginRequestConverter 表单登录请求 token 颁发支持 ..
     */
    @Bean
    public LightningAppAuthServerConfigurer appAuthServerConfigurer() {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(ApplicationAuthServerConfigurer<HttpSecurity> applicationAuthServerConfigurer) throws Exception {
                applicationAuthServerConfigurer.tokenEndpoint(endpoint -> {
                    endpoint.addAccessTokenRequestConverter(new FormLoginRequestConverter());
                });
            }
        };
    }


    @Bean
    public LightningAuthServerConfigurer FormLoginAuthenticationEntryPointConfigurer(
            LightningSecurityContextRepository securityContextRepository
    ) {
        return new LightningAuthServerConfigurer() {
            @Override
            public void configure(HttpSecurity securityBuilder) throws Exception {
                FormLoginConfigurer<HttpSecurity> httpSecurityFormLoginConfigurer = securityBuilder.formLogin();

                // entry point
                LightningAuthenticationEntryPoint authenticationEntryPoint
                        = AppAuthConfigurerUtils.getAuthenticationEntryPoint(securityBuilder);

                httpSecurityFormLoginConfigurer
                        .successHandler(authenticationEntryPoint)
                        .failureHandler(authenticationEntryPoint)
                        .and()
                        .sessionManagement()
                        // 无状态session 会话
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                        // 也就是 securityContext 通过 resource server 进行加载
                        .securityContext()
                        .securityContextRepository(securityContextRepository)
                        .and()
                        .exceptionHandling()
                         // 处理认证 entry Point ...
                        .authenticationEntryPoint(authenticationEntryPoint);

                // 启动登录页面 ...
                ApplicationFormLoginConfiguration.logoutWithLogin(
                        httpSecurityFormLoginConfigurer,
                        formLoginProperties.getNoSeparation(),
                        ApplicationAuthServerUtils.getApplicationAuthServerProperties(securityBuilder)
                );

                // 禁用掉默认的登出页面 ...
                securityBuilder.csrf().disable();

            }
        };
    }
}
