package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.LightningOAuth2LoginConfigurer;
import com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningGrantedAuthoritiesMapper;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2AuthorizationExtRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized.LightningAnonymousOAuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.oauthorized.LightningOAuthorizedClientService;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

@Configuration
@ConditionalOnProperty(prefix = "lightning.auth.app.server.config.oauth2-login", name = "enable", havingValue = "true")
@EnableConfigurationProperties(OAuth2LoginProperties.class)
@Import({
        ApplicationClientRegistrationConfiguration.class, ApplicationAuthorizationRequestConfiguration.class,
        ApplicationAuthorizedClientConfiguration.class
})
public class ApplicationOAuth2LoginConfiguration {

    private final AuthorizationExtEndpointConfig authorizationExtEndpointConfig = new AuthorizationExtEndpointConfig();


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
        return new LightningOAuth2LoginAuthenticationEntryPoint();
    }

    @Bean
    public LightningOAuth2LoginConfigurer lightningOAuth2LoginConfigurer(
            OAuth2LoginProperties properties,
            @Qualifier("oauth2")
            AuthenticationSuccessHandler authenticationSuccessHandler,
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

                if (properties.getIsSeparation()) {

                    // backend separation config

                    OAuth2LoginProperties.BackendSeparation backendSeparation = properties.getBackendSeparation();
                    oAuth2LoginConfigurer.successHandler(authenticationSuccessHandler);
                    oAuth2LoginConfigurer.failureHandler(authenticationFailureHandler);

                    // success handler

                    // exception handler ..

                    if (authenticationSuccessHandler instanceof LightningOAuth2LoginAuthenticationEntryPoint point) {
                        if (StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
                            point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
                        }
                    }

                    if (authenticationFailureHandler instanceof LightningOAuth2LoginAuthenticationEntryPoint point) {

                        if (backendSeparation.getEnableAuthErrorDetail()) {
                            point.setEnableAuthErrorDetails(Boolean.TRUE);
                        }
                        if (StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
                            point.setAuthErrorMessage(backendSeparation.getLoginFailureMessage());
                        }

                    }

                } else {
                    OAuth2LoginProperties.NoSeparation noSeparation = properties.getNoSeparation();
                    if (StringUtils.hasText(properties.getLoginProcessUrl())) {
                        oAuth2LoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                    }
                    // success handler

                    // exception handler ..

                    // 不开启session ,所以需要一个默认实现 ..
                    if (StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
                        oAuth2LoginConfigurer.successHandler(
                                new ForwardAuthenticationSuccessHandler(noSeparation.getDefaultSuccessUrl())
                        );
                    } else {
                        // fallback
                        oAuth2LoginConfigurer.successHandler(
                                new ForwardAuthenticationSuccessHandler("/")
                        );
                    }

                    // fail url handle
                    if (StringUtils.hasText(noSeparation.getDefaultFailureUrl())) {
                        oAuth2LoginConfigurer.failureUrl(noSeparation.getDefaultFailureUrl());
                    }


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
