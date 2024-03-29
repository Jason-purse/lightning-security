package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfigurer;
import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.LightningAppAuthServerConfigurer;
import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2GrantedAuthoritiesMapper;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.DefaultOAuth2LoginAccessTokenAuthenticationConverter;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.OAuth2ClientLoginAccessTokenAuthenticationConverter;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestRepository;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningOAuth2AuthorizationRequestResolver;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningAnonymousOAuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuthorizedClientService;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 可以 需要 一个token 生成器 (作为oauth2 client 登陆时）
 * <p>
 * 同样还也需要一个 token 解析器
 */
@Configuration
@AutoConfigureAfter({OAuth2ClientAutoConfiguration.class})
@Import({ApplicationOAuth2LoginComponentsImportSelector.class})
public class ApplicationOAuth2LoginConfiguration {


    @EnableConfigurationProperties(OAuth2LoginProperties.class)
    @RequiredArgsConstructor
    public static class OAuth2LoginConfiguration {


        /**
         * 由于默认使用的是 oidc
         *
         * @return
         */
        @Bean
        @ConditionalOnMissingBean(LightningOidcUserService.class)
        public LightningOidcUserService oidcUserService() {
            return new DefaultLightningOidcUserService(new LightningOidcUserService() {
                private final OidcUserService delegate = new OidcUserService();

                @Override
                public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
                    return delegate.loadUser(userRequest);
                }
            });
        }


        @Bean
        public LightningAuthServerConfigurer lightningOAuth2LoginConfigurer(
                OAuth2LoginProperties properties,
                ApplicationAuthServerProperties authServerProperties,
                @Autowired(required = false)
                        LightningAuthorizationRequestRepository repository,
                @Autowired(required = false)
                        LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper,
                @Autowired(required = false)
                        LightningOAuth2AuthorizationRequestResolver requestResolver,
                @Autowired(required = false)
                        LightningOAuthorizedClientService oAuthorizedClientService,
                @Autowired(required = false)
                        LightningAnonymousOAuthorizedClientRepository anonymousOAuthorizedClientRepository,
                @Autowired(required = false)
                        LightningOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) {
            return new LightningAuthServerConfigurer() {
                @Override
                public void configure(HttpSecurity securityBuilder) throws Exception {
                    OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer = securityBuilder.oauth2Login();
                    List<String> patterns = new LinkedList<>();

                    if (!authServerProperties.isSeparation()) {
                        noSeparationConfig(oAuth2LoginConfigurer, patterns, properties);
                    } else {
                        if (authServerProperties.getBackendSeparation().isEnableLoginPage()) {
                            OAuth2LoginUtils.configDefaultLoginPageGeneratorFilter(
                                    securityBuilder,
                                    properties.getNoSeparation().getLoginPageUrl()
                            );
                        }
                    }

                    // 授权放行url
                    urlWhiteList(oAuth2LoginConfigurer, patterns);

                    // 默认的情况下,我们需要处理 认证转换结果 ..(让它能够产生Token)
                    //使用 token 端点来表示登录处理Url ..
                    //不管是否为前后端分离登录,都可以使用token 端点作为登录处理url
                    if (StringUtils.hasText(properties.getLoginProcessUrl())) {
                        oAuth2LoginConfigurer.loginProcessingUrl(properties.getLoginProcessUrl());
                    }

                    oauth2AuthorizationCodeFlowConfig(oAuth2LoginConfigurer, properties, repository, requestResolver);

                    userInfoEndpointConfig(securityBuilder, oAuth2LoginConfigurer, authoritiesMapper, oAuthorizedClientService, anonymousOAuthorizedClientRepository);

                    redirectConfig(oAuth2LoginConfigurer, properties);

                    tokenAccessConfig(oAuth2LoginConfigurer, accessTokenResponseClient);

                    // 设置 clientRegistrationRepository ..
                    // 默认不会影响 spring oauth2 client的相关配置 ..
                    oAuth2LoginConfigurer.clientRegistrationRepository(OAuth2LoginUtils.getClientRegistrationRepository(securityBuilder));
                }


                private static void tokenAccessConfig(OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer, LightningOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) {
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

                private static void userInfoEndpointConfig(
                        HttpSecurity securityBuilder,
                        OAuth2LoginConfigurer<HttpSecurity> oAuth2LoginConfigurer,
                        LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper,
                        LightningOAuthorizedClientService oAuthorizedClientService,
                        LightningAnonymousOAuthorizedClientRepository anonymousOAuthorizedClientRepository) {
                    // userinfo endpoint com.generatera.oauth2.resource.server.config
                    OAuth2LoginConfigurer<HttpSecurity>.UserInfoEndpointConfig userInfoEndpointConfig
                            = oAuth2LoginConfigurer.userInfoEndpoint();
                    if (authoritiesMapper != null) {
                        userInfoEndpointConfig.userAuthoritiesMapper(authoritiesMapper);
                    }

                    userInfoEndpointConfig.userService(OAuth2LoginUtils.getOauth2UserService(securityBuilder));
                    userInfoEndpointConfig.oidcUserService(OAuth2LoginUtils.getOidcUserService(securityBuilder));

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

                    // 授权请求处理 ..
                    if (StringUtils.hasText(oAuthorizationRequestEndpoint.getAuthorizationRequestBaseUri())) {
                        authorizationEndpointConfig.baseUri(oAuthorizationRequestEndpoint.getAuthorizationRequestBaseUri());
                        OAuth2ClientLoginAccessTokenAuthenticationConverter accessTokenAuthenticationConverter = OAuth2LoginUtils.getOAuth2LoginAccessTokenAuthenticationConverter(oAuth2LoginConfigurer.and());
                        if (accessTokenAuthenticationConverter instanceof DefaultOAuth2LoginAccessTokenAuthenticationConverter converter) {
                            converter.setRedirectBaseUri(oAuthorizationRequestEndpoint.getAuthorizationRequestBaseUri());
                        }
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
                    if (StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                        // 自定义登录页面
                        oAuth2LoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                        patterns.add(noSeparation.getLoginPageUrl());
                    } else {
                        // 默认登录页面的处理 ...
                        OAuth2LoginUtils.configDefaultLoginPageGeneratorFilter(
                                oAuth2LoginConfigurer.and(),
                                properties.getNoSeparation().getLoginPageUrl());
                    }
                }
            };
        }
    }

    /**
     * 增加到 oauth2 request converter ,让token filter 支持 ..
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public LightningAppAuthServerConfigurer appAuthServerConfigurer() {
        return new LightningAppAuthServerConfigurer() {
            @Override
            public void configure(ApplicationAuthServerConfigurer<HttpSecurity> applicationAuthServerConfigurer) throws Exception {
                applicationAuthServerConfigurer.tokenEndpoint(token -> {
                    token.addAccessTokenRequestConverter(OAuth2LoginUtils.getOAuth2LoginAccessTokenAuthenticationConverter(applicationAuthServerConfigurer.and()));
                });
            }
        };
    }

}
