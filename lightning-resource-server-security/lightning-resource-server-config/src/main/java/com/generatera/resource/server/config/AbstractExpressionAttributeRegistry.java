package com.generatera.resource.server.config;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractExpressionAttributeRegistry<T extends ExpressionAttribute> {
    private final Map<MethodClassKey, T> cachedAttributes = new ConcurrentHashMap();

    AbstractExpressionAttributeRegistry() {
    }

    final T getAttribute(MethodInvocation mi) {
        Method method = mi.getMethod();
        Object target = mi.getThis();
        Class<?> targetClass = target != null ? target.getClass() : null;
        return this.getAttribute(method, targetClass);
    }

    final T getAttribute(Method method, Class<?> targetClass) {
        MethodClassKey cacheKey = new MethodClassKey(method, targetClass);
        return (T)this.cachedAttributes.computeIfAbsent(cacheKey, (k) -> {
            return this.resolveAttribute(method, targetClass);
        });
    }

    @NonNull
    abstract T resolveAttribute(Method method, Class<?> targetClass);
}