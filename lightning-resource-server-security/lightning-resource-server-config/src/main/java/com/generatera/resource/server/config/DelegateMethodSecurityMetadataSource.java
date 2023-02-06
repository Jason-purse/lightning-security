package com.generatera.resource.server.config;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.MethodSecurityMetadataSource;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 14:05
 * @Description 代理方法安全元数据源 ..
 */
public class DelegateMethodSecurityMetadataSource implements MethodSecurityMetadataSource {

    private final ThreadLocal<MethodSecurityMetadataSource> localSupport = new ThreadLocal<>();


    private final List<MethodSecurityMetadataSource> delegate;

    public DelegateMethodSecurityMetadataSource(List<MethodSecurityMetadataSource> delegate) {
        this.delegate = delegate;
    }

    public DelegateMethodSecurityMetadataSource(MethodSecurityMetadataSource... delegate) {
        this.delegate = List.of(delegate);
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        return handleBy(methodSecurityMetadataSource -> methodSecurityMetadataSource.getAttributes(method, targetClass));
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        return handleBy(methodSecurityMetadataSource -> methodSecurityMetadataSource.getAttributes(object));
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return handleBy(MethodSecurityMetadataSource::getAllConfigAttributes);
    }

    private <T> T handleBy(Function<MethodSecurityMetadataSource, T> action) {
        MethodSecurityMetadataSource methodSecurityMetadataSource = localSupport.get();
        // 设置移除 ...
        if (methodSecurityMetadataSource == null) {
            localSupport.remove();
        }
        return methodSecurityMetadataSource != null ? action.apply(methodSecurityMetadataSource) : null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        for (MethodSecurityMetadataSource methodSecurityMetadataSource : delegate) {
            if (methodSecurityMetadataSource.supports(clazz)) {
                localSupport.set(methodSecurityMetadataSource);
                return true;
            }
        }

        return false;
    }


}
