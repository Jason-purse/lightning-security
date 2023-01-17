package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.server.common.configuration.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.config.authentication.RedirectAuthenticationSuccessOrFailureHandler;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2GrantedAuthoritiesMapper;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2AuthorizationExtRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningOAuth2AuthorizationRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningAnonymousOAuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuthorizedClientService;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.generatera.authorization.application.server.oauth2.login.config.user.OidcUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 可以 需要 一个token 生成器 (作为oauth2 client 登陆时）
 * <p>
 * 同样还也需要一个 token 解析器
 */
@Configuration
@AutoConfigureBefore(ApplicationAuthServerConfig.class)
@Import({ApplicationOAuth2LoginComponentsImportSelector.class})
public class ApplicationOAuth2LoginConfiguration {

    @Configuration
    @EnableConfigurationProperties(OAuth2LoginProperties.class)
    @RequiredArgsConstructor
    public static class OAuth2LoginConfiguration {
        private final AuthorizationExtEndpointConfig authorizationExtEndpointConfig = new AuthorizationExtEndpointConfig();


        @Bean
        @ConditionalOnMissingBean(LightningOidcUserService.class)
        public LightningOidcUserService oidcUserService() {
            return new LightningOidcUserService() {
                private final OidcUserService oidcUserService = new OidcUserService();

                @Override
                public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
                    return new OidcUserDetails(oidcUserService.loadUser(userRequest));
                }
            };
        }



        @Bean
        public LightningAppAuthServerConfigurer lightningOAuth2LoginConfigurer(
                OAuth2LoginProperties properties,
                @Autowired(required = false)
                        LightningAuthorizationRequestRepository repository,
                @Autowired(required = false)
                        LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper,
                @Autowired(required = false)
                        LightningOAuth2AuthorizationRequestResolver requestResolver,
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
            return new LightningAppAuthServerConfigurer() {
                @Override
                public void configure(HttpSecurity securityBuilder) throws Exception {
                    OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer = securityBuilder.oauth2Login();
                    List<String> patterns = new LinkedList<>();
                    if (!properties.getIsSeparation()) {
                        noSeparationConfig(oAuth2LoginConfigurer, patterns, properties);
                    }
                    // 授权放行url
                    urlWhiteList(oAuth2LoginConfigurer, patterns);


                    if (StringUtils.hasText(properties.getLoginProcessUrl())) {
                        oAuth2LoginConfigurer.loginProcessingUrl(properties.getLoginProcessUrl());
                    }

                    oauth2AuthorizationCodeFlowConfig(oAuth2LoginConfigurer, properties, repository, requestResolver);

                    userInfoEndpointConfig(oAuth2LoginConfigurer, authoritiesMapper, oAuth2UserService, oidcUserService, oAuthorizedClientService, anonymousOAuthorizedClientRepository);

                    redirectConfig(oAuth2LoginConfigurer, properties);

                    tokenAccessConfig(oAuth2LoginConfigurer, accessTokenResponseClient);
                }


                private static void tokenAccessConfig(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, LightningOAuth2AccessTokenResponseClient accessTokenResponseClient) {
                    // token endpoint com.generatera.oauth2.resource.server.config
                    OAuth2LoginConfigurer<HttpSecurity>.TokenEndpointConfig tokenEndpointConfig = oAuth2LoginConfigurer.tokenEndpoint();
                    if (accessTokenResponseClient != null) {
                        tokenEndpointConfig.accessTokenResponseClient(accessTokenResponseClient);
                    }
                }

                private static void redirectConfig(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, OAuth2LoginProperties properties) {
                    // redirection endpoint com.generatera.oauth2.resource.server.config
                    OAuth2LoginConfigurer<HttpSecurity>.RedirectionEndpointConfig redirectionEndpointConfig =
                            oAuth2LoginConfigurer.redirectionEndpoint();

                    OAuth2LoginProperties.RedirectionEndpoint redirectionEndpoint = properties.getRedirectionEndpoint();
                    if (StringUtils.hasText(redirectionEndpoint.getBaseUrl())) {
                        redirectionEndpointConfig.baseUri(redirectionEndpoint.getBaseUrl());
                    }
                }

                private static void userInfoEndpointConfig(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper, LightningOAuth2UserService oAuth2UserService, LightningOidcUserService oidcUserService, LightningOAuthorizedClientService oAuthorizedClientService, LightningAnonymousOAuthorizedClientRepository anonymousOAuthorizedClientRepository) {
                    // userinfo endpoint com.generatera.oauth2.resource.server.config
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
                }

                private static void oauth2AuthorizationCodeFlowConfig(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, OAuth2LoginProperties properties, LightningAuthorizationRequestRepository repository, LightningOAuth2AuthorizationRequestResolver requestResolver) {
                    // authorization code flow for component

                    // authorization endpoint com.generatera.oauth2.resource.server.config
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
                }

                private static void urlWhiteList(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, List<String> patterns) {
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
                }

                private static void noSeparationConfig(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, List<String> patterns, OAuth2LoginProperties properties) {
                    OAuth2LoginProperties.NoSeparation noSeparation = properties.getNoSeparation();
                    if (StringUtils.hasText(properties.getLoginProcessUrl())) {
                        oAuth2LoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                    }

                    if (noSeparation.getEnableSavedRequestForward() != null && noSeparation.getEnableSavedRequestForward()) {
                        if (StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
                            oAuth2LoginConfigurer.defaultSuccessUrl(noSeparation.getDefaultSuccessUrl());
                        }
                    } else {
                        if (noSeparation.getEnableForward() != null && noSeparation.getEnableForward()) {

                            enableForward(oAuth2LoginConfigurer, patterns, noSeparation);
                        } else {
                            enableRedirect(oAuth2LoginConfigurer, patterns, noSeparation);
                        }
                    }
                }

                private static void enableRedirect(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, List<String> patterns, OAuth2LoginProperties.NoSeparation noSeparation) {
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
                    } else {
                        if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                            oAuth2LoginConfigurer.failureHandler(
                                    new RedirectAuthenticationSuccessOrFailureHandler(
                                            noSeparation.getLoginPageUrl())
                            );
                        } else {
                            // fall back
                            oAuth2LoginConfigurer.failureHandler(
                                    new RedirectAuthenticationSuccessOrFailureHandler(
                                            "/login"
                                    )
                            );
                        }

                    }
                }

                private static void enableForward(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, List<String> patterns, OAuth2LoginProperties.NoSeparation noSeparation) {
                    if (StringUtils.hasText(noSeparation.getSuccessUrl())) {
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
