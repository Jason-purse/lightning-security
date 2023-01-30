package com.generatera.authorization.application.server.form.login.config.util;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.util.StringUtils;

import java.util.Map;

public class FormLoginUtils {

    public static <B extends HttpSecurityBuilder<B>> void configDefaultLoginPageGeneratorFilter(B builder, String loginPage, String logoutSuccessUrl) {

        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = builder.getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter != null) {
            ApplicationAuthServerProperties loginProperties = builder.getSharedObject(ApplicationAuthServerProperties.class);
            // 不强制要求重定向到登录页面 ...(但是需要放行对应的页面才行) ...
            String failureUrl = ElvisUtil.stringElvis(loginProperties.getNoSeparation().getFailureForwardOrRedirectUrl(), "/login?error");
            loginPageGeneratingFilter.setFormLoginEnabled(true);
            loginPageGeneratingFilter.setLoginPageUrl(loginPage);
            loginPageGeneratingFilter.setFailureUrl(failureUrl);
            if (StringUtils.hasText(logoutSuccessUrl)) {
                loginPageGeneratingFilter.setLogoutSuccessUrl(logoutSuccessUrl);
            }
            builder.addFilter(loginPageGeneratingFilter);
        }
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
