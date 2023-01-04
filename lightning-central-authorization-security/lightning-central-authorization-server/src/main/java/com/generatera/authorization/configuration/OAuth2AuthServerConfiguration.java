package com.generatera.authorization.configuration;

import com.generatera.authorization.application.server.config.LightningOAuth2ServerConfigurer;
import com.generatera.authorization.ext.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import com.generatera.authorization.ext.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2TokenEndpointConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;

import java.util.Arrays;

@Configuration
public class OAuth2AuthServerConfiguration {
    /**
     * 自定义授权同意页面
     */
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";



    // ---------------------------------- Authorization Grant support -----------------------------------------------

    // 实现 Resource Owner Password Credentials
    // 默认配置已经支持(oauth2 client 直接发起 resource owner password 获取 access  token 请求)
    // 但是oauth2.1 草案不支持 resource owner 模式
    // 自己添加
    // 详情查看 org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration

    /**
     * 中央认证服务中心配置 ..
     */
    @Bean("lightning.oauth2.auth.server.configurer")
    public LightningOAuth2ServerConfigurer oAuth2ServerConfigurer() {
        return new LightningOAuth2ServerConfigurer() {
            @Override
            public void configure(OAuth2AuthorizationServerConfigurer<HttpSecurity> oAuth2AuthorizationServerConfigurer) {
                // oauth2 server configuration
                oAuth2AuthorizationServerConfigurer.tokenEndpoint(
                        new Customizer<OAuth2TokenEndpointConfigurer>() {
                            @Override
                            public void customize(OAuth2TokenEndpointConfigurer oAuth2TokenEndpointConfigurer) {
                                // 修改访问token 请求转换器(让它支持 自定义密码模式)
                                oAuth2TokenEndpointConfigurer.accessTokenRequestConverter(
                                        new DelegatingAuthenticationConverter(Arrays.asList(
                                                new OAuth2AuthorizationCodeAuthenticationConverter(), // 授权码模式
                                                new OAuth2RefreshTokenAuthenticationConverter(),  // 刷新token
                                                new OAuth2ClientCredentialsAuthenticationConverter(),  // 客户端模式
                                                new OAuth2ResourceOwnerPasswordAuthenticationConverter())) // 自定义密码模式
                                );
                                // 自定义授权同意页面
                                oAuth2AuthorizationServerConfigurer.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI));
                            }
                        }
                );

                // resource owner password authentication provider
                try {
                    addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(oAuth2AuthorizationServerConfigurer.and());
                }catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }

    /**
     * 自定义密码模式
     *
     * @param http httpSecurity
     */
    @SuppressWarnings("unchecked")
    private void addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(HttpSecurity http) throws Exception {

        http.apply(new SecurityConfigurerAdapter<>() {
            @Override
            public void configure(HttpSecurity builder) {
                AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
                OAuth2AuthorizationService authorizationService = http.getSharedObject(OAuth2AuthorizationService.class);
                OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = http.getSharedObject(OAuth2TokenGenerator.class);
                // oauth2.0 resourceOwnerPassword authentication support
                AuthenticationProvider authenticationProvider = new OAuth2ResourceOwnerPasswordAuthenticationProvider(authenticationManager, authorizationService, tokenGenerator);
                http.authenticationProvider(authenticationProvider);
            }
        });
    }


}
