package com.generatera.security.authorization.server.specification.util;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class HttpSecurityBuilderUtils {


    public static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type, Function<B, T> defaultSupplier) {
        T object = builder.getSharedObject(type);
        if (object == null) {
            object = getOptionalBean(builder, type);
            if (object == null) {
                object = defaultSupplier.apply(builder);
            }
            Assert.notNull(object, "the default supplier cannot provide null !!!");
            builder.setSharedObject(type, object);
        }
        return object;
    }

    public static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type, Supplier<T> defaultSupplier) {
        return getBean(builder, type, security -> defaultSupplier.get());
    }

    public static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, Class<T> type) {
        return builder.getSharedObject(ApplicationContext.class).getBean(type);
    }

    public static <B extends HttpSecurityBuilder<B>, T> T getBean(B builder, ResolvableType type) {
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

    public static <B extends HttpSecurityBuilder<B>,T> T getSharedOrCtxBean(B builder,Class<T> tClass) {

        T bean = builder.getSharedObject(tClass);
        if(bean == null) {
            bean = getBean(builder, tClass);
            builder.setSharedObject(tClass,bean);
        }
        return bean;
    }


    public static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(builder.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            int var10003 = beansMap.size();
            String var10004 = type.getName();
            throw new NoUniqueBeanDefinitionException(type, var10003, "Expected single matching bean of type '" + var10004 + "' but found " + beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        } else {
            return !beansMap.isEmpty() ? beansMap.values().iterator().next() : null;
        }
    }

    public static <B extends HttpSecurityBuilder<B>, T> T getOptionalBean(B builder, ResolvableType type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        } else {
            return names.length == 1 ? (T) context.getBean(names[0]) : null;
        }
    }


    public static <B extends HttpSecurityBuilder<B>, T> Collection<T> getBeansForType(B builder, Class<T> type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] beanNamesForType = context.getBeanNamesForType(type);
        if (beanNamesForType.length > 0) {
            return context.getBeansOfType(type).values();
        }

        return Collections.emptyList();
    }

    public static <B extends HttpSecurityBuilder<B>, T> int getBeanNameSizesForType(B builder, Class<T> type) {
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        String[] beanNamesForType = context.getBeanNamesForType(type);
        return beanNamesForType.length;
    }

}
