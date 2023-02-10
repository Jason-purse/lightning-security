package com.generatera.resource.server.config.method.security;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
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
public class DelegateLightningExtMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource implements LightningExtMethodSecurityMetadataSource {


    private final List<LightningExtMethodSecurityMetadataSource> delegate;

    private final boolean cacheFlag;

    public DelegateLightningExtMethodSecurityMetadataSource(List<LightningExtMethodSecurityMetadataSource> delegate) {
        this.delegate = delegate;
        for (LightningExtMethodSecurityMetadataSource lightningExtMethodSecurityMetadataSource : delegate) {
            if (!lightningExtMethodSecurityMetadataSource.isCacheable()) {
                cacheFlag = false;
                return;
            }
        }
        cacheFlag = true;
    }

    public DelegateLightningExtMethodSecurityMetadataSource(LightningExtMethodSecurityMetadataSource... delegate) {
        this(List.of(delegate));
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        return handleBy(methodSecurityMetadataSource -> methodSecurityMetadataSource.getAttributes(method, targetClass));
    }


    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return handleBy(MethodSecurityMetadataSource::getAllConfigAttributes);
    }

    private <T> T handleBy(Function<MethodSecurityMetadataSource, T> action) {
        for (MethodSecurityMetadataSource source : delegate) {
            T apply = action.apply(source);
            if (apply != null) {
                return apply;
            }
        }
        return null;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        for (LightningExtMethodSecurityMetadataSource lightningExtMethodSecurityMetadataSource : delegate) {
            if (!lightningExtMethodSecurityMetadataSource.isCacheable()) {
                lightningExtMethodSecurityMetadataSource.onApplicationEvent(event);
            }
        }
    }

    @Override
    public boolean isCacheable() {
       return cacheFlag;
    }
}
