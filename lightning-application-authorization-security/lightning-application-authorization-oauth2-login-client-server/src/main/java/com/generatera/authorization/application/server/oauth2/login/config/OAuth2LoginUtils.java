package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.oauth2.login.config.authentication.DefaultLightningOAuth2LoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    public static LightningOAuth2LoginAuthenticationEntryPoint getOAuth2LoginAuthenticationEntryPoint(HttpSecurity builder) {
        LightningOAuth2LoginAuthenticationEntryPoint authenticationEntryPoint = builder.getSharedObject(LightningOAuth2LoginAuthenticationEntryPoint.class);
        if(authenticationEntryPoint == null) {

            // 尝试从bean 工厂中获取
            LightningOAuth2LoginAuthenticationEntryPoint bean = getOptionalBean(builder, LightningOAuth2LoginAuthenticationEntryPoint.class);
            authenticationEntryPoint = bean;
            if(bean == null) {
                DefaultLightningOAuth2LoginAuthenticationEntryPoint entryPoint = new DefaultLightningOAuth2LoginAuthenticationEntryPoint();
                builder.setSharedObject(LightningOAuth2LoginAuthenticationEntryPoint.class,entryPoint);
                authenticationEntryPoint = entryPoint;
            }
        }

        // 认证entry point
        return authenticationEntryPoint;
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
