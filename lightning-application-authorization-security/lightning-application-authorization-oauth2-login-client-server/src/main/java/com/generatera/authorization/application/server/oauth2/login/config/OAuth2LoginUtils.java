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
import com.generatera.authorization.application.server.oauth2.login.config.client.register.LightningOAuth2ClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.token.LightningOAuth2AuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.DefaultOAuth2AccessTokenResponseClient;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils.getSharedOrCtxBean;

/**
 * oauth2 login utils
 *
 * 提供默认值
 * {@link com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint}
 *
 */
public class OAuth2LoginUtils {

    public static <B extends HttpSecurityBuilder<B>> void configDefaultLoginPageGeneratorFilter(B builder, String loginPage) {

        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = builder.getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter != null) {
            if(!loginPageGeneratingFilter.isEnabled()) {
                builder.addFilter(loginPageGeneratingFilter);
            }
            ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
            // 不强制要求重定向到登录页面 ...(但是需要放行对应的页面才行) ...
            loginPageGeneratingFilter.setOauth2LoginEnabled(true);
            loginPageGeneratingFilter.setLoginPageUrl(ElvisUtil.stringElvis(loginPage,applicationAuthServerUtils.getProperties().getNoSeparation().getLoginPageUrl()));
            loginPageGeneratingFilter.setFailureUrl(applicationAuthServerUtils.getProperties().getNoSeparation().getFailureForwardOrRedirectUrl());
            String logoutSuccessUrl = applicationAuthServerUtils.getProperties().getNoSeparation().getLogoutSuccessUrl();
            loginPageGeneratingFilter.setLogoutSuccessUrl(logoutSuccessUrl);
        }
    }

    public static LightningOAuth2AccessTokenResponseClient getOAuth2AccessTokenResponseClient(HttpSecurity security) {
        LightningOAuth2AccessTokenResponseClient tokenResponseClient = security.getSharedObject(LightningOAuth2AccessTokenResponseClient.class);
        if(tokenResponseClient == null) {
            tokenResponseClient = getOptionalBean(security,LightningOAuth2AccessTokenResponseClient.class);
            if(tokenResponseClient == null) {
                tokenResponseClient = new DefaultOAuth2AccessTokenResponseClient();
            }
            security.setSharedObject(LightningOAuth2AccessTokenResponseClient.class,tokenResponseClient);
        }
        return tokenResponseClient;
    }

    public static <B extends HttpSecurityBuilder<B>> LightningOAuth2UserService getOauth2UserService(B builder) {
        LightningOAuth2UserService oAuth2UserService = builder.getSharedObject(LightningOAuth2UserService.class);
        if(oAuth2UserService == null) {
            oAuth2UserService = getOptionalBean(builder, LightningOAuth2UserService.class);
            if(oAuth2UserService == null) {
                oAuth2UserService = new DefaultLightningOAuth2UserService(new DefaultOAuth2UserService());
                builder.setSharedObject(LightningOAuth2UserService.class,oAuth2UserService);
            }
        }
        return oAuth2UserService;
    }

    public static <B extends HttpSecurityBuilder<B>> LightningOidcUserService getOidcUserService(B builder) {
        LightningOidcUserService oAuth2UserService = builder.getSharedObject(LightningOidcUserService.class);
        if(oAuth2UserService == null) {
            oAuth2UserService = getOptionalBean(builder, LightningOidcUserService.class);
            if(oAuth2UserService == null) {
                oAuth2UserService = new DefaultLightningOidcUserService(new OidcUserService());
            }
            builder.setSharedObject(LightningOidcUserService.class,oAuth2UserService);
        }
        return oAuth2UserService;
    }

/**
 * @author FLJ
 * @date 2023/3/10
 * @time 10:41
 * @Description oauth2 login access token 认证转换器 ...
 */
    public static <B extends HttpSecurityBuilder<B>>  OAuth2ClientLoginAccessTokenAuthenticationConverter getOAuth2LoginAccessTokenAuthenticationConverter(B builder) {
        OAuth2ClientLoginAccessTokenAuthenticationConverter oAuth2ClientLoginAccessTokenAuthenticationConverter = builder.getSharedObject(OAuth2ClientLoginAccessTokenAuthenticationConverter.class);
        if(oAuth2ClientLoginAccessTokenAuthenticationConverter == null) {
            oAuth2ClientLoginAccessTokenAuthenticationConverter = getOptionalBean(builder, OAuth2ClientLoginAccessTokenAuthenticationConverter.class);

            if(oAuth2ClientLoginAccessTokenAuthenticationConverter == null) {
                oAuth2ClientLoginAccessTokenAuthenticationConverter = new OAuth2LoginAccessTokenAuthenticationConverter(getClientRegistrationRepository(builder));
            }
            // 设置转换器 ...
            builder.setSharedObject(OAuth2ClientLoginAccessTokenAuthenticationConverter.class,oAuth2ClientLoginAccessTokenAuthenticationConverter);
        }
        return oAuth2ClientLoginAccessTokenAuthenticationConverter;
    }

    public static LightningOAuth2AuthenticationEntryPoint getAuthenticationEntryPoint(HttpSecurity securityBuilder) {
        LightningOAuth2AuthenticationEntryPoint sharedObject = securityBuilder.getSharedObject(LightningOAuth2AuthenticationEntryPoint.class);
        if(sharedObject == null) {
            sharedObject = getOptionalBean(securityBuilder, LightningOAuth2AuthenticationEntryPoint.class);
            if(sharedObject == null) {
                sharedObject = new DefaultOAuth2ClientAuthenticationEntryPoint(
                        AppAuthConfigurerUtils.getAppAuthServerForTokenAuthenticationProvider(securityBuilder));
            }
            securityBuilder.setSharedObject(LightningOAuth2AuthenticationEntryPoint.class,sharedObject);
        }

        return sharedObject;
    }

    public static <B extends HttpSecurityBuilder<B>>  LightningOAuth2ClientRegistrationRepository getClientRegistrationRepository(B securityBuilder) {
        return getSharedOrCtxBean(securityBuilder, LightningOAuth2ClientRegistrationRepository.class);
    }


    static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(builder.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            int var10003 = beansMap.size();
            String var10004 = type.getName();
            throw new NoUniqueBeanDefinitionException(type, var10003, "Expected single matching bean of type '" + var10004 + "' but found " + beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        } else {
            return !beansMap.isEmpty() ? beansMap.values().iterator().next() : null;
        }
    }

    static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, ResolvableType type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        } else {
            return names.length == 1 ? (T) context.getBean(names[0]) : null;
        }
    }

    static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type) {
        return builder.getSharedObject(ApplicationContext.class).getBean(type);
    }

    static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, ResolvableType type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length == 1) {
            return (T) context.getBean(names[0]);
        } else if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        } else {
            throw new NoSuchBeanDefinitionException(type);
        }
    }



}
