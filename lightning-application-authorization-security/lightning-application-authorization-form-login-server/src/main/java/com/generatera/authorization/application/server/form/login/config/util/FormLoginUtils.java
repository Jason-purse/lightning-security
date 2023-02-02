package com.generatera.authorization.application.server.form.login.config.util;

import com.generatera.authorization.application.server.config.util.ApplicationAuthServerUtils;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.util.StringUtils;

import java.util.Map;

public class FormLoginUtils {

    public static <B extends HttpSecurityBuilder<B>> void configDefaultLoginPageGeneratorFilter(B builder, String loginPage) {

        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = builder.getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter != null) {
            // 如果已经被启用了,那么就不需要增加
            if(!loginPageGeneratingFilter.isEnabled()) {
                builder.addFilter(loginPageGeneratingFilter);
            }
            ApplicationAuthServerUtils applicationAuthServerUtils = ApplicationAuthServerUtils.getApplicationAuthServerProperties(builder);
            // 不强制要求重定向到登录页面 ...(但是需要放行对应的页面才行) ...
            loginPageGeneratingFilter.setFormLoginEnabled(true);
            loginPageGeneratingFilter.setLoginPageUrl(ElvisUtil.stringElvis(loginPage,applicationAuthServerUtils.getProperties().getNoSeparation().getLoginPageUrl()));
            loginPageGeneratingFilter.setFailureUrl(applicationAuthServerUtils.getProperties().getNoSeparation().getFailureForwardOrRedirectUrl());
            String logoutSuccessUrl = applicationAuthServerUtils.getProperties().getNoSeparation().getLogoutSuccessUrl();
            if (StringUtils.hasText(logoutSuccessUrl)) {
                loginPageGeneratingFilter.setLogoutSuccessUrl(logoutSuccessUrl);
            }
        }
    }

    @Nullable
    public static <B extends HttpSecurityBuilder<B>> UserDetailsService getUserDetailsService(B builder) {
        UserDetailsService sharedObject = builder.getSharedObject(UserDetailsService.class);
        if (sharedObject == null) {
            UserDetailsService bean = getBean(builder, UserDetailsService.class);
            builder.setSharedObject(UserDetailsService.class, bean);
            sharedObject = bean;
        }
        return sharedObject;
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
