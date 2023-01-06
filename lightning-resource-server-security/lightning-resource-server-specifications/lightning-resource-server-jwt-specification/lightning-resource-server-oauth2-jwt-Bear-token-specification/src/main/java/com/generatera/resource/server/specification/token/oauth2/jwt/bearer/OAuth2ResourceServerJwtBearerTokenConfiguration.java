package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import com.generatera.resource.server.config.LightningResourceServerOtherConfigurer;
import com.generatera.resource.server.config.ResourceServerConfiguration;
import com.generatera.resource.server.config.token.entrypoint.DefaultForbiddenAuthenticationEntryPoint;
import com.generatera.resource.server.config.token.entrypoint.LightningAuthenticationEntryPoint;
import com.generatera.resource.server.config.util.AuthHttpResponseUtil;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * oauth2 resource server 本身有自己的 Filter 所以我们只需要配置它的Filter 即可 ..
 *
 * 如果使用默认配置就是 support JWT-encoded Bearer Tokens
 */
@Configuration
@EnableConfigurationProperties(OAuth2JwtBearerTokenResourceServerProperties.class)
@AutoConfigureAfter(ResourceServerConfiguration.class)
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
                    OAuth2BearerTokenAuthenticationManagerResolver authenticationManagerResolver,
            @Autowired(required = false)
            OAuth2JwtBearerTokenDecoder jwtDecoder,
            @Autowired(required = false)
            OAuth2JwtAuthenticationConverter oAuth2JwtAuthenticationConverter,
            @Autowired(required = false)
            OAuth2JwtBearerTokenAuthenticationManager authenticationManager,
            OAuth2JwtBearerTokenResourceServerProperties resourceServerProperties

    ) {
        return new LightningResourceServerOtherConfigurer() {
            @Override
            public void configure(HttpSecurity httpSecurity) {
                try {
                    httpSecurity.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                        @Override
                        public void init(HttpSecurity builder) throws Exception {
                            // 在 oauth2 resource 配置器之前处理 ..
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
                            else {
                                configurer.authenticationEntryPoint(OAuth2BearerTokenConfigurerUtils.getAuthenticationEntryPoint(configurer.and()));
                            }


                            // 认证管理器默认应该也是使用全局的认证管理器 ..
                            if (authenticationManagerResolver != null) {
                                configurer.authenticationManagerResolver(authenticationManagerResolver);
                            }

                            // 默认先启动jwt
                            OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwt = configurer.jwt();
                            // jwt 可选配置 ..
                            jwtOptionalConfig(jwt);


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
                        }
                    });
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }

            private void jwtOptionalConfig(OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwt) {

                // 可选覆盖 ..

                if(jwtDecoder != null) {
                    jwt.decoder(jwtDecoder);
                }

                if(oAuth2JwtAuthenticationConverter != null) {
                    jwt.jwtAuthenticationConverter(oAuth2JwtAuthenticationConverter);
                }

                if(StringUtils.hasText(resourceServerProperties.getAuthorizationServerInfoSettings().getIssuer())) {
                    jwt.jwkSetUri(resourceServerProperties.getAuthorizationServerInfoSettings().getIssuer());
                }
                if(authenticationManager != null) {
                    jwt.authenticationManager(authenticationManager);
                }
            }
        };
    }

}
