package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.DefaultOAuth2ClientAuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.OAuth2ClientLoginAccessTokenAuthenticationConverter;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.OAuth2LoginAccessTokenAuthenticationConverter;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2ClientConfigurerExtUtils;
import com.generatera.authorization.application.server.oauth2.login.config.client.register.LightningOAuth2ClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.token.LightningOAuth2AuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.DefaultOAuth2AccessTokenForOAuthorizeCodeResponseClient;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

import static com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils.getOptionalBean;
import static com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils.getSharedOrCtxBean;

/**
 *  oauth2 login utils
 *
 *  oauth2 login application authorization server??? ?????????
 * {@link com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint}
 * @see OAuth2ClientConfigurerExtUtils
 */
public class OAuth2LoginUtils {

    public static <B extends HttpSecurityBuilder<B>> void configDefaultLoginPageGeneratorFilter(B builder, String loginPage) {

        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = builder.getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter != null) {
            if (!loginPageGeneratingFilter.isEnabled()) {
                builder.addFilter(loginPageGeneratingFilter);
            }
            ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
            // ??????????????????????????????????????? ...(???????????????????????????????????????) ...
            loginPageGeneratingFilter.setOauth2LoginEnabled(true);
            loginPageGeneratingFilter.setLoginPageUrl(ElvisUtil.stringElvis(loginPage, applicationAuthServerUtils.getProperties().getNoSeparation().getLoginPageUrl()));
            loginPageGeneratingFilter.setFailureUrl(applicationAuthServerUtils.getProperties().getNoSeparation().getFailureForwardOrRedirectUrl());
            String logoutSuccessUrl = applicationAuthServerUtils.getProperties().getNoSeparation().getLogoutSuccessUrl();
            loginPageGeneratingFilter.setLogoutSuccessUrl(logoutSuccessUrl);
        }
    }

    /**
     * ?????????????????????   LightningOAuth2AccessTokenResponseClient
     */
    public static LightningOAuth2AccessTokenResponseClient getOAuth2AccessTokenResponseClient(HttpSecurity security) {
        LightningOAuth2AccessTokenResponseClient tokenResponseClient = security.getSharedObject(LightningOAuth2AccessTokenResponseClient.class);
        if (tokenResponseClient == null) {
            tokenResponseClient = getOptionalBean(security, LightningOAuth2AccessTokenResponseClient.class);
            if (tokenResponseClient == null) {
                tokenResponseClient = new DefaultOAuth2AccessTokenForOAuthorizeCodeResponseClient();
            }
            security.setSharedObject(LightningOAuth2AccessTokenResponseClient.class, tokenResponseClient);
        }
        return tokenResponseClient;
    }

    public static <B extends HttpSecurityBuilder<B>> LightningOAuth2UserService getOauth2UserService(B builder) {
        LightningOAuth2UserService oAuth2UserService = builder.getSharedObject(LightningOAuth2UserService.class);
        if (oAuth2UserService == null) {
            oAuth2UserService = getOptionalBean(builder, LightningOAuth2UserService.class);
            if (oAuth2UserService == null) {
                oAuth2UserService = new DefaultLightningOAuth2UserService(new DefaultOAuth2UserService());
                builder.setSharedObject(LightningOAuth2UserService.class, oAuth2UserService);
            }
        }
        return oAuth2UserService;
    }

    public static <B extends HttpSecurityBuilder<B>> LightningOidcUserService getOidcUserService(B builder) {
        LightningOidcUserService oAuth2UserService = builder.getSharedObject(LightningOidcUserService.class);
        if (oAuth2UserService == null) {
            oAuth2UserService = getOptionalBean(builder, LightningOidcUserService.class);
            if (oAuth2UserService == null) {
                oAuth2UserService = new DefaultLightningOidcUserService(new OidcUserService());
            }
            builder.setSharedObject(LightningOidcUserService.class, oAuth2UserService);
        }
        return oAuth2UserService;
    }

    /**
     * @author FLJ
     * @date 2023/3/10
     * @time 10:41
     * @Description oauth2 login access token ??????????????? ...
     */
    public static <B extends HttpSecurityBuilder<B>> OAuth2ClientLoginAccessTokenAuthenticationConverter getOAuth2LoginAccessTokenAuthenticationConverter(B builder) {
        OAuth2ClientLoginAccessTokenAuthenticationConverter oAuth2ClientLoginAccessTokenAuthenticationConverter = builder.getSharedObject(OAuth2ClientLoginAccessTokenAuthenticationConverter.class);
        if (oAuth2ClientLoginAccessTokenAuthenticationConverter == null) {
            oAuth2ClientLoginAccessTokenAuthenticationConverter = getOptionalBean(builder, OAuth2ClientLoginAccessTokenAuthenticationConverter.class);

            if (oAuth2ClientLoginAccessTokenAuthenticationConverter == null) {
                oAuth2ClientLoginAccessTokenAuthenticationConverter = new OAuth2LoginAccessTokenAuthenticationConverter(getClientRegistrationRepository(builder));
            }
            // ??????????????? ...
            builder.setSharedObject(OAuth2ClientLoginAccessTokenAuthenticationConverter.class, oAuth2ClientLoginAccessTokenAuthenticationConverter);
        }
        return oAuth2ClientLoginAccessTokenAuthenticationConverter;
    }

    public static LightningOAuth2AuthenticationEntryPoint getAuthenticationEntryPoint(HttpSecurity securityBuilder) {
        LightningOAuth2AuthenticationEntryPoint sharedObject = securityBuilder.getSharedObject(LightningOAuth2AuthenticationEntryPoint.class);
        if (sharedObject == null) {
            sharedObject = getOptionalBean(securityBuilder, LightningOAuth2AuthenticationEntryPoint.class);
            if (sharedObject == null) {
                sharedObject = new DefaultOAuth2ClientAuthenticationEntryPoint(
                        AppAuthConfigurerUtils.getAppAuthServerForTokenAuthenticationProvider(securityBuilder));
            }
            securityBuilder.setSharedObject(LightningOAuth2AuthenticationEntryPoint.class, sharedObject);
        }

        return sharedObject;
    }

    public static <B extends HttpSecurityBuilder<B>> LightningOAuth2ClientRegistrationRepository getClientRegistrationRepository(B securityBuilder) {
        return getSharedOrCtxBean(securityBuilder, LightningOAuth2ClientRegistrationRepository.class);
    }

    /**
     * ??????????????? OAuth2AuthorizedClientManager
     */
    public static <B extends HttpSecurityBuilder<B>> OAuth2AuthorizedClientManager getOAuth2AuthorizedClientManager(B securityBuilder) {
        return HttpSecurityBuilderUtils.getBean(securityBuilder, OAuth2AuthorizedClientManager.class, () -> {
            OAuth2AuthorizedClientManager authorizedClientManager = null;
            LightningOAuth2ClientRegistrationRepository registrationRepository = getClientRegistrationRepository(securityBuilder);

            LightningOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient = getOptionalBean(securityBuilder, ResolvableType.forClassWithGenerics(LightningOAuth2AccessTokenResponseClient.class, OAuth2ClientCredentialsGrantRequest.class));
            if (accessTokenResponseClient != null) {
                OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().authorizationCode().refreshToken().clientCredentials((configurer) -> {
                    configurer.accessTokenResponseClient(accessTokenResponseClient);
                }).password().build();
                DefaultOAuth2AuthorizedClientManager defaultAuthorizedClientManager = new DefaultOAuth2AuthorizedClientManager(registrationRepository, OAuth2ClientConfigurerExtUtils.getAuthorizedClientRepository(securityBuilder));
                defaultAuthorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
                authorizedClientManager = defaultAuthorizedClientManager;
            } else {
                authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(registrationRepository, OAuth2ClientConfigurerExtUtils.getAuthorizedClientRepository(securityBuilder));
            }
            return authorizedClientManager;
        });
    }

}
