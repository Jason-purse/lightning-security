package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.LightningOAuth2LoginConfigurer;
import com.generatera.authorization.application.server.config.RedirectAuthenticationSuccessOrFailureHandler;
import com.generatera.authorization.application.server.config.specification.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningGrantedAuthoritiesMapper;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2AuthorizationExtRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized.LightningAnonymousOAuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized.LightningOAuthorizedClientService;
import com.generatera.authorization.application.server.oauth2.login.config.token.DefaultOAuth2LoginAuthenticationTokenGenerator;
import com.generatera.authorization.application.server.oauth2.login.config.token.LightningOAuth2LoginAuthenticationTokenGenerator;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.generatera.authorization.application.server.oauth2.login.config.user.OidcUserPrincipal;
import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationTokenParser;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 可以 需要 一个token 生成器 (作为oauth2 client 登陆时）
 *
 * 同样还也需要一个 token 解析器
 *
 *
 */
@Configuration
@AutoConfigureAfter(ApplicationAuthServerConfig.class)
@Import({OAuth2LoginConfigurationImportSelector.class,ApplicationOAuth2LoginComponentsImportSelector.class})
public class ApplicationOAuth2LoginConfiguration {

    @EnableConfigurationProperties(OAuth2LoginProperties.class)
    @RequiredArgsConstructor
    public static class OAuth2LoginConfiguration {
        private final AuthorizationExtEndpointConfig authorizationExtEndpointConfig = new AuthorizationExtEndpointConfig();
        private final OAuth2LoginProperties oAuth2LoginProperties;

        /**
         * 当前后端不分离的时候,不需要它 ..
         */
        @Autowired(required = false)
        private  LightningAuthenticationTokenService authenticationTokenService;


        @Autowired(required = false)
        private LightningOAuth2LoginAuthenticationTokenGenerator tokenGenerator;

        /**
         * TODO 分配token 解析任务
         */
        @Autowired(required = false)
        private LightningAuthenticationTokenParser tokenParser;


        private final JWKSource<SecurityContext> jwkSource;

        @Bean
        public LightningOidcUserService oidcUserService() {
            return new LightningOidcUserService() {
                private final OidcUserService oidcUserService = new OidcUserService();
                @Override
                public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
                    return new OidcUserPrincipal(oidcUserService.loadUser(userRequest));
                }
            };
        }

        @Bean
        @Qualifier("oauth2")
        @ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
        public AuthenticationSuccessHandler authenticationSuccessHandler() {
            return entryPoint();
        }

        @Bean
        @Qualifier("oauth2")
        @ConditionalOnMissingBean(AuthenticationFailureHandler.class)
        public AuthenticationFailureHandler authenticationFailureHandler() {
            return entryPoint();
        }

        private LightningOAuth2LoginAuthenticationEntryPoint entryPoint() {
            LightningOAuth2LoginAuthenticationEntryPoint point = new LightningOAuth2LoginAuthenticationEntryPoint();
            OAuth2LoginProperties.BackendSeparation backendSeparation = oAuth2LoginProperties.getBackendSeparation();
            if (StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
                point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
            }
            if (backendSeparation.getEnableAuthErrorDetail()) {
                point.setEnableAuthErrorDetails(Boolean.TRUE);
            }
            if (StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
                point.setAuthErrorMessage(backendSeparation.getLoginFailureMessage());
            }
            Assert.notNull(authenticationTokenService,"authenticationTokenService must not be null !!!");
            point.setAuthenticationTokenService(authenticationTokenService);
            // 必须存在
            point.setTokenGenerator(Objects.requireNonNullElseGet(tokenGenerator, () -> new DefaultOAuth2LoginAuthenticationTokenGenerator(jwkSource)));

            return point;
        }

        @Bean
        public LightningOAuth2LoginConfigurer lightningOAuth2LoginConfigurer(
                OAuth2LoginProperties properties,
                @Autowired(required = false)
                @Qualifier("oauth2")
                AuthenticationSuccessHandler authenticationSuccessHandler,
                @Autowired(required = false)
                @Qualifier("oauth2")
                AuthenticationFailureHandler authenticationFailureHandler,
                @Autowired(required = false)
                LightningAuthorizationRequestRepository repository,
                @Autowired(required = false)
                LightningGrantedAuthoritiesMapper authoritiesMapper,
                @Autowired(required = false)
                LightningAuthorizationRequestResolver requestResolver,
                @Autowired(required = false)
                LightningOAuth2UserService oAuth2UserService,
                @Autowired(required = false)
                LightningOidcUserService oidcUserService,
                @Autowired(required = false)
                LightningOAuthorizedClientService oAuthorizedClientService,
                @Autowired(required = false)
                LightningAnonymousOAuthorizedClientRepository anonymousOAuthorizedClientRepository,
                @Autowired(required = false)
                LightningOAuth2AccessTokenResponseClient accessTokenResponseClient) {
            return new LightningOAuth2LoginConfigurer() {
                @Override
                public void configure(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer) {
                    List<String> patterns = new LinkedList<>();
                    if (properties.getIsSeparation()) {

                        // backend separation config
                        // success handler
                        // exception handler ..
                        oAuth2LoginConfigurer.successHandler(authenticationSuccessHandler);
                        oAuth2LoginConfigurer.failureHandler(authenticationFailureHandler);
                    } else {
                        OAuth2LoginProperties.NoSeparation noSeparation = properties.getNoSeparation();
                        if (StringUtils.hasText(properties.getLoginProcessUrl())) {
                            oAuth2LoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                        }

                        // success handler
                        // exception handler ..
                        if(noSeparation.getEnableSavedRequestForward() != null && noSeparation.getEnableSavedRequestForward()) {
                            if (StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
                                oAuth2LoginConfigurer.defaultSuccessUrl(noSeparation.getDefaultSuccessUrl());
                            }
                        }
                        else {
                            if(noSeparation.getEnableForward()!= null && noSeparation.getEnableForward()) {

                                if(StringUtils.hasText(noSeparation.getSuccessUrl())) {
                                    oAuth2LoginConfigurer.successHandler(
                                            new ForwardAuthenticationSuccessHandler(noSeparation.getSuccessUrl())
                                    );
                                    patterns.add(noSeparation.getSuccessUrl());
                                }

                                // fail url handle
                                if (StringUtils.hasText(noSeparation.getFailureUrl())) {
                                    oAuth2LoginConfigurer.failureUrl(noSeparation.getFailureUrl());
                                    patterns.add(noSeparation.getFailureUrl());
                                }
                            }
                            else {
                                // 不开启session ,所以需要一个默认实现 ..
                                if (StringUtils.hasText(noSeparation.getSuccessUrl())) {
                                    oAuth2LoginConfigurer.successHandler(
                                            new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getSuccessUrl())
                                    );

                                    patterns.add(noSeparation.getSuccessUrl());
                                } else {
                                    // fallback
                                    oAuth2LoginConfigurer.successHandler(
                                            new RedirectAuthenticationSuccessOrFailureHandler("/")
                                    );
                                    patterns.add("/");
                                }

                                // fail url handle
                                if (StringUtils.hasText(noSeparation.getFailureUrl())) {
                                    oAuth2LoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getFailureUrl()));
                                    patterns.add(noSeparation.getFailureUrl());
                                }
                                else {
                                    if(StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                                        oAuth2LoginConfigurer.failureHandler(
                                                new RedirectAuthenticationSuccessOrFailureHandler(
                                                        noSeparation.getLoginPageUrl())
                                        );
                                    }

                                    else {
                                        // fall back
                                        oAuth2LoginConfigurer.failureHandler(
                                                new RedirectAuthenticationSuccessOrFailureHandler(
                                                        "/login"
                                                )
                                        );
                                    }

                                }
                            }
                        }

                    }

                    // 授权放行url
                    try {
                        oAuth2LoginConfigurer.and()
                                .authorizeHttpRequests()
                                .antMatchers(
                                     patterns.toArray(String[]::new)
                                )
                                .permitAll();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }


                    if (StringUtils.hasText(properties.getLoginProcessUrl())) {
                        oAuth2LoginConfigurer.loginProcessingUrl(properties.getLoginProcessUrl());
                    }

                    // authorization code flow for component

                    // authorization endpoint config
                    OAuth2LoginConfigurer<HttpSecurity>.AuthorizationEndpointConfig authorizationEndpointConfig
                            = oAuth2LoginConfigurer.authorizationEndpoint();
                    OAuth2LoginProperties.OAuthorizationRequestEndpoint oAuthorizationRequestEndpoint =
                            properties.getAuthorizationRequestEndpoint();
                    if (StringUtils.hasText(oAuthorizationRequestEndpoint.getAuthorizationRequestBaseUri())) {
                        authorizationEndpointConfig.baseUri(oAuthorizationRequestEndpoint.getAuthorizationRequestBaseUri());
                    }
                    // 为空  使用默认的
                    if (repository != null) {
                        authorizationEndpointConfig.authorizationRequestRepository(repository);
                    }

                    if (requestResolver != null) {
                        authorizationEndpointConfig.authorizationRequestResolver(requestResolver);
                    }

                    // userinfo endpoint config
                    OAuth2LoginConfigurer<HttpSecurity>.UserInfoEndpointConfig userInfoEndpointConfig
                            = oAuth2LoginConfigurer.userInfoEndpoint();
                    if (authoritiesMapper != null) {
                        userInfoEndpointConfig.userAuthoritiesMapper(authoritiesMapper);
                    }

                    if (oAuth2UserService != null) {
                        userInfoEndpointConfig.userService(oAuth2UserService);
                    }
                    if (oidcUserService != null) {
                        userInfoEndpointConfig.oidcUserService(oidcUserService);
                    }

                    if (oAuthorizedClientService != null) {
                        AuthenticatedPrincipalOAuth2AuthorizedClientRepository authorizedClientRepository
                                = new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(oAuthorizedClientService);
                        if (anonymousOAuthorizedClientRepository != null) {
                            authorizedClientRepository.setAnonymousAuthorizedClientRepository(anonymousOAuthorizedClientRepository);
                        }
                        oAuth2LoginConfigurer.authorizedClientRepository(authorizedClientRepository);

                    }

                    // redirection endpoint config
                    OAuth2LoginConfigurer<HttpSecurity>.RedirectionEndpointConfig redirectionEndpointConfig =
                            oAuth2LoginConfigurer.redirectionEndpoint();

                    OAuth2LoginProperties.RedirectionEndpoint redirectionEndpoint = properties.getRedirectionEndpoint();
                    if (StringUtils.hasText(redirectionEndpoint.getBaseUrl())) {
                        redirectionEndpointConfig.baseUri(redirectionEndpoint.getBaseUrl());
                    }

                    // token endpoint config
                    OAuth2LoginConfigurer<HttpSecurity>.TokenEndpointConfig tokenEndpointConfig = oAuth2LoginConfigurer.tokenEndpoint();
                    if (accessTokenResponseClient != null) {
                        tokenEndpointConfig.accessTokenResponseClient(accessTokenResponseClient);
                    }
                }
            };
        }


        public void authorizationExtEndpoint(Consumer<AuthorizationExtEndpointConfig> configConsumer) {
            configConsumer.accept(authorizationExtEndpointConfig);
        }


        public static final class AuthorizationExtEndpointConfig {
            private String authorizationExtRequestBaseUri;
            private OAuth2AuthorizationExtRequestResolver authorizationExtRequestResolver;

            private AuthorizationExtEndpointConfig() {
            }

            public AuthorizationExtEndpointConfig baseUri(String authorizationExtRequestBaseUri) {
                Assert.hasText(authorizationExtRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
                this.authorizationExtRequestBaseUri = authorizationExtRequestBaseUri;
                return this;
            }

            public AuthorizationExtEndpointConfig authorizationExtRequestResolver(OAuth2AuthorizationExtRequestResolver authorizationRequestResolver) {
                Assert.notNull(authorizationRequestResolver, "authorizationRequestResolver cannot be null");
                this.authorizationExtRequestResolver = authorizationRequestResolver;
                return this;
            }

        }
    }

}
