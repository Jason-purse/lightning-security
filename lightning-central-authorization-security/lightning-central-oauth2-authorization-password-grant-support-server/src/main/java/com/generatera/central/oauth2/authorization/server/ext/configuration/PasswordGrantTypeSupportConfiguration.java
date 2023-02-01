package com.generatera.central.oauth2.authorization.server.ext.configuration;

import com.generatera.authorization.server.common.configuration.util.LogUtil;
import com.generatera.central.oauth2.authorization.server.configuration.LightningOAuth2CentralAuthorizationServerBootstrapConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;

import java.util.Arrays;

/**
 * password 授权类型支持扩展
 */
@Configuration
@AutoConfiguration
public class PasswordGrantTypeSupportConfiguration {

    @Bean
    public LightningOAuth2CentralAuthorizationServerBootstrapConfigurer passwordGrantConfigurer() {
        return new LightningOAuth2CentralAuthorizationServerBootstrapConfigurer() {
            @Override
            public void configure(OAuth2AuthorizationServerConfigurer<HttpSecurity> configurer) throws Exception {
                configurer.tokenEndpoint( tokenEndpoint -> {
                    tokenEndpoint.accessTokenRequestConverter(
                            new DelegatingAuthenticationConverter(
                                    Arrays.asList(
                                            // 授权码
                                            new OAuth2AuthorizationCodeAuthenticationConverter(),
                                            // 客户端凭证
                                            new OAuth2ClientCredentialsAuthenticationConverter(),
                                            // 刷新token
                                            new OAuth2RefreshTokenAuthenticationConverter(),
                                            // password
                                            new OAuth2ResourceOwnerPasswordAuthenticationConverter()
                                    )
                            )
                    );
                });

                addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(configurer.and());

                LogUtil.prettyLog("OAuth2 Authorization Server add Resource Owner Grant Type authorization ...");
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
