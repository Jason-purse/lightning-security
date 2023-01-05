package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import com.generatera.resource.server.config.LightningResourceServerOtherConfigurer;
import com.generatera.resource.server.specification.token.jwt.bearer.ResourceServerJwtBearerTokenConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;

/**
 * oauth2 resource server 本身有自己的 Filter 所以我们只需要配置它的Filter 即可 ..
 *
 * 如果使用默认配置就是 support JWT-encoded Bearer Tokens
 */
@Configuration
@AutoConfigureBefore(ResourceServerJwtBearerTokenConfiguration.class)
public class OAuth2ResourceServerJwtBearerTokenConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    public OAuth2BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter() {
        return new OAuth2BearerTokenAuthenticationFilter();
    }

    @Bean
    public LightningResourceServerOtherConfigurer oauth2FilterConfigurer(
            @Autowired(required = false)
                    OAuth2BearerTokenAccessDeniedHandler accessDeniedHandler,
            @Autowired(required = false)
                    OAuth2BearerAuthenticationTokenResolver tokenResolver,
            @Autowired(required = false)
                    OAuth2BearerTokenAuthenticationEntryPoint authenticationEntryPoint,
            @Autowired(required = false)
                    OAuth2BearerTokenAuthenticationManagerResolver authenticationManagerResolver

    ) {
        return new LightningResourceServerOtherConfigurer() {
            @Override
            public void configure(HttpSecurity httpSecurity) {
                try {
                    OAuth2ResourceServerConfigurer<HttpSecurity> configurer = httpSecurity
                            .oauth2ResourceServer();
                    if (accessDeniedHandler != null) {
                        configurer.accessDeniedHandler(accessDeniedHandler);
                    }
                    if (tokenResolver != null) {
                        configurer.bearerTokenResolver(tokenResolver);
                    }

                    if (authenticationEntryPoint != null) {
                        configurer.authenticationEntryPoint(authenticationEntryPoint);
                    }

                    // 认证管理器默认应该也是使用全局的认证管理器 ..
                    if (authenticationManagerResolver != null) {
                        configurer.authenticationManagerResolver(authenticationManagerResolver);
                    }

                    // 默认先启动jwt
                    configurer.jwt();

                    // wrap
                    configurer
                            .addObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                                @Override
                                public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                                    // 包装 BearerTokenAuthenticationFilter
                                    OAuth2BearerTokenAuthenticationFilterProvider.setFilter(filter);
                                    return filter;
                                }
                            });

                    configurer.and()
                            .authorizeHttpRequests()
                            .anyRequest()
                            .authenticated();
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }

}
