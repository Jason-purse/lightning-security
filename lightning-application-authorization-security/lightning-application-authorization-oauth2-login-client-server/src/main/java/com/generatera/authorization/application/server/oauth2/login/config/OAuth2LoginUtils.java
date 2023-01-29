package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * oauth2 login utils
 *
 * 提供默认值
 * {@link com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint}
 *
 */
public class OAuth2LoginUtils {

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
                builder.setSharedObject(LightningOidcUserService.class,oAuth2UserService);
            }
        }
        return oAuth2UserService;
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
