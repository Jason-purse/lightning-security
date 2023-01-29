package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.authentication.LightningAppAuthServerDaoLoginAuthenticationProvider;
import com.generatera.authorization.application.server.config.token.AuthConfigurerUtils;
import com.generatera.authorization.application.server.form.login.config.authentication.DefaultLightningFormLoginAuthenticationEntryPoint;
import com.generatera.authorization.application.server.form.login.config.authentication.LightningFormLoginAuthenticationEntryPoint;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.util.StringUtils;

import java.util.Map;

public class FormLoginUtils {

    public static LightningFormLoginAuthenticationEntryPoint getFormLoginAuthenticationEntryPoint(HttpSecurity builder) {
        LightningFormLoginAuthenticationEntryPoint authenticationEntryPoint = builder.getSharedObject(LightningFormLoginAuthenticationEntryPoint.class);
        if(authenticationEntryPoint == null) {
            // 尝试从bean 工厂中获取
            LightningFormLoginAuthenticationEntryPoint bean = getOptionalBean(builder, LightningFormLoginAuthenticationEntryPoint.class);
            authenticationEntryPoint = bean;
            if(bean == null) {
                DefaultLightningFormLoginAuthenticationEntryPoint entryPoint = new DefaultLightningFormLoginAuthenticationEntryPoint();
                FormLoginProperties loginProperties = builder.getSharedObject(FormLoginProperties.class);
                entryPointConfig(entryPoint,loginProperties);
                builder.setSharedObject(LightningFormLoginAuthenticationEntryPoint.class,entryPoint);
                authenticationEntryPoint = entryPoint;
            }
        }

        // 认证entry point
        return authenticationEntryPoint;
    }

    private static void entryPointConfig(DefaultLightningFormLoginAuthenticationEntryPoint point,FormLoginProperties properties) {
        FormLoginProperties.BackendSeparation backendSeparation = properties.getBackendSeparation();

        if (StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
            point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
        }

        point.setEnableAccountStatusInform(backendSeparation.getEnableAccountStatusInform());

        if (ObjectUtils.isNotEmpty(backendSeparation.getEnableAccountStatusInform()) && backendSeparation.getEnableAccountStatusInform()) {
            if (StringUtils.hasText(backendSeparation.getAccountLockedMessage())) {
                point.setAccountStatusLockedMessage(backendSeparation.getAccountLockedMessage());
            }
            if (StringUtils.hasText(backendSeparation.getAccountExpiredMessage())) {
                point.setAccountStatusExpiredMessage(backendSeparation.getAccountExpiredMessage());
            }
        }

        if (StringUtils.hasText(backendSeparation.getAccountStatusMessage())) {
            point.setAccountStatusMessage(backendSeparation.getAccountStatusMessage());
        }

        if (StringUtils.hasText(backendSeparation.getBadCredentialMessage())) {
            point.setBadCredentialsMessage(backendSeparation.getBadCredentialMessage());
        }

        if (StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
            point.setLoginFailureMessage(backendSeparation.getLoginFailureMessage());
        }
    }

    /**
     * 配置出一个具有正常dao功能的provider,并且附加了token生成的功能 ...
     */
    public static AuthenticationProvider getOptmizedAuthenticationProvider(HttpSecurity securityBuilder) {
        // 进行token 生成的provider
        LightningAppAuthServerDaoLoginAuthenticationProvider accessAuthenticationProvider = securityBuilder.getSharedObject(LightningAppAuthServerDaoLoginAuthenticationProvider.class);
        if(accessAuthenticationProvider == null) {
            accessAuthenticationProvider = getOptionalBean(securityBuilder, LightningAppAuthServerDaoLoginAuthenticationProvider.class);
            if(accessAuthenticationProvider == null) {
                ApplicationAuthServerProperties authServerProperties = securityBuilder.getSharedObject(ApplicationAuthServerProperties.class);
                accessAuthenticationProvider = new LightningAppAuthServerDaoLoginAuthenticationProvider(
                        AuthConfigurerUtils.getAppAuthServerForTokenAuthenticationProvider(securityBuilder),
                        // todo 修改设置properties(根据前后端分离配置处理）
                        false
                );
            }
            securityBuilder.setSharedObject(LightningAppAuthServerDaoLoginAuthenticationProvider.class,accessAuthenticationProvider);
        }
        return accessAuthenticationProvider;
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
