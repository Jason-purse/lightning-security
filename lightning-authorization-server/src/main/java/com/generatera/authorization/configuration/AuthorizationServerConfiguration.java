package com.generatera.authorization.configuration;

import com.generatera.authorization.configuration.jose.Jwks;
import com.generatera.authorization.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import com.generatera.authorization.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationProvider;
import com.generatera.authorization.oauth2.customizer.jwt.JwtCustomizer;
import com.generatera.authorization.oauth2.customizer.jwt.JwtCustomizerHandler;
import com.generatera.authorization.oauth2.customizer.jwt.impl.JwtCustomizerImpl;
import com.generatera.authorization.oauth2.customizer.token.claims.OAuth2TokenClaimsCustomizer;
import com.generatera.authorization.oauth2.customizer.token.claims.impl.OAuth2TokenClaimsCustomizerImpl;
import com.generatera.authorization.oauth2.repository.OAuth2AuthorizationConsentRepository;
import com.generatera.authorization.oauth2.repository.OAuth2AuthorizationRepository;
import com.generatera.authorization.oauth2.service.impl.JpaOAuth2AuthorizationConsentService;
import com.generatera.authorization.oauth2.service.impl.JpaOAuth2AuthorizationService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.AuthenticationException;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 授权服务配置
 *
 * @author weir
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfiguration {

    /**
     * 自定义授权同意页面
     */
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";
    /**
     * 授权服务颁发者-配置引入
     */
    @Value("${oauth2.token.issuer}")
    private String tokenIssuer;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer<HttpSecurity>()
                .tokenEndpoint(oAuth2TokenEndpointConfigurer -> {
                    oAuth2TokenEndpointConfigurer.accessTokenRequestConverter(
                            new DelegatingAuthenticationConverter(Arrays.asList(
                                    new OAuth2AuthorizationCodeAuthenticationConverter(), // 授权码模式
                                    new OAuth2RefreshTokenAuthenticationConverter(),  // 刷新token
                                    new OAuth2ClientCredentialsAuthenticationConverter(),  // 客户端模式
                                    new OAuth2ResourceOwnerPasswordAuthenticationConverter())) // 自定义密码模式
                    );
                })
                // 自定义授权同意页面
                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI));



        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        DefaultSecurityFilterChain securityChain =
                http
                        .apply(authorizationServerConfigurer)
                        .and()
                        // 自定义联名身份
                        //.apply(new FederatedIdentityConfigurer())
                        //.and()
                        .authorizeHttpRequests()
                        .mvcMatchers("/springauthserver/oauth2/**")
                        .permitAll()
                        //.antMatchers(HttpMethod.GET,"/login")
                        //.permitAll()
                        .anyRequest()
                        //.authenticated()
                        .permitAll()
                        .and()
                        .csrf()
                        .ignoringRequestMatchers(endpointsMatcher)
                        .and()
                        .formLogin()
                        .failureHandler(new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                response.getWriter().write("登录失败 !!!");
                            }
                        })
                        .and()
                        .exceptionHandling()
                        .authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                                System.out.println("登录失败");
                                response.getOutputStream().write("登录失败".getBytes());
                            }
                        })
                        .and()
                        .build();

        /**
         * Custom configuration for Resource Owner Password grant type.
         * Current implementation has no support for Resource Owner
         * Password grant type  自定义密码模式
         */
        addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(http);

        return securityChain;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(OAuth2AuthorizationRepository oauth2AuthorizationRepository, RegisteredClientRepository registeredClientRepository) {
        return new JpaOAuth2AuthorizationService(oauth2AuthorizationRepository, registeredClientRepository);
    }

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

    @Bean
    public ProviderSettings authorizationServerSettings() {
        return ProviderSettings.builder().issuer(tokenIssuer).build();
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

    /**
     * 自定义密码模式
     *
     * @param http httpSecurity
     */
    @SuppressWarnings("unchecked")
    private void addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(HttpSecurity http) {

        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        OAuth2AuthorizationService authorizationService = http.getSharedObject(OAuth2AuthorizationService.class);
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = http.getSharedObject(OAuth2TokenGenerator.class);


        OAuth2ResourceOwnerPasswordAuthenticationProvider resourceOwnerPasswordAuthenticationProvider =
                new OAuth2ResourceOwnerPasswordAuthenticationProvider(authenticationManager, authorizationService, tokenGenerator);


        // This will add new authentication provider in the list of existing authentication providers.
        http.authenticationProvider(resourceOwnerPasswordAuthenticationProvider);
    }

}
