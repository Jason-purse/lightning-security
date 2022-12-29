package com.generatera.authorization.configuration;

import com.generatera.authorization.configuration.jose.Jwks;
import com.generatera.authorization.ext.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import com.generatera.authorization.ext.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationProvider;
import com.generatera.authorization.ext.oauth2.customizer.jwt.JwtCustomizer;
import com.generatera.authorization.ext.oauth2.customizer.jwt.JwtCustomizerHandler;
import com.generatera.authorization.ext.oauth2.customizer.jwt.impl.JwtCustomizerImpl;
import com.generatera.authorization.ext.oauth2.customizer.token.claims.OAuth2TokenClaimsCustomizer;
import com.generatera.authorization.ext.oauth2.customizer.token.claims.impl.OAuth2TokenClaimsCustomizerImpl;
import com.generatera.authorization.model.constant.ProviderSettingProperties;
import com.generatera.authorization.service.OAuth2AuthorizationConsentRepository;
import com.generatera.authorization.service.impl.JpaOAuth2AuthorizationConsentService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2TokenEndpointConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

/**
 * 授权服务器配置
 * <p>
 * 有关oauth2 login,我们需要详细的几个组件 ..
 * 1. 用于查询已经注册的客户端的客户端仓库(RegisteredClientRepository)
 * 2. RegisteredClientService 本质上同 RegisteredClientRepository 一样的作用 ..
 *
 * @author FLJ
 */
@EnableConfigurationProperties({ProviderSettingProperties.class})
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfiguration {

    /**
     * 自定义授权同意页面
     */
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    /**
     * 提供者配置属性
     */
    @Autowired
    private ProviderSettingProperties properties;



    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(OAuth2AuthorizationConsentRepository oauth2AuthorizationConsentRepository, RegisteredClientRepository registeredClientRepository) {
        return new JpaOAuth2AuthorizationConsentService(oauth2AuthorizationConsentRepository, registeredClientRepository);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 当前我们自己第三方 授权服务提供商的一些端点配置 ..
     *
     * @return provider config
     */
    @Bean
    public ProviderSettings authorizationServerSettings() {

        final ProviderSettings.Builder builder = ProviderSettings
                .builder();

        // issuer 可以自动生成
        if (StringUtils.isNotBlank(properties.getIssuer())) {
            builder.issuer(properties.getIssuer());
        }
        return builder
                .authorizationEndpoint(properties.getAuthorizationEndpoint())
                .tokenEndpoint(properties.getTokenEndpoint())
                .jwkSetEndpoint(properties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(properties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(properties.getTokenIntrospectionEndpoint())
                .oidcClientRegistrationEndpoint(properties.getOidcClientRegistrationEndpoint())
                .oidcUserInfoEndpoint(properties.getOidcUserInfoEndpoint())
                .build();

    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> buildJwtCustomizer() {

        JwtCustomizerHandler jwtCustomizerHandler = JwtCustomizerHandler.getJwtCustomizerHandler();
        JwtCustomizer jwtCustomizer = new JwtCustomizerImpl(jwtCustomizerHandler);

        return jwtCustomizer::customizeToken;
    }

    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> buildOAuth2TokenClaimsCustomizer() {

        OAuth2TokenClaimsCustomizer oauth2TokenClaimsCustomizer = new OAuth2TokenClaimsCustomizerImpl();

        return oauth2TokenClaimsCustomizer::customizeTokenClaims;
    }


    // ---------------------------------- Authorization Grant support -----------------------------------------------

    // 实现 Resource Owner Password Credentials
    // 默认配置已经支持(oauth2 client 直接发起 resource owner password 获取 access  token 请求)
    // 但是oauth2.1 草案不支持 resource owner 模式
    // 自己添加
    // 详情查看 org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        // oauth2 server configuration
        OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer<HttpSecurity>()
                .tokenEndpoint(new Customizer<OAuth2TokenEndpointConfigurer>() {
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
                    }
                })
                // 自定义授权同意页面
                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI));


        // csrf handle
        http
                .apply(authorizationServerConfigurer)
                .and()
                .csrf()
                .disable();
        addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(http);
        // 仅仅拦截这一部分的 ..
        http.requestMatchers()
                .antMatchers("/auth/**");


        return http.build();
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
            public void configure(HttpSecurity builder) throws Exception {
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
