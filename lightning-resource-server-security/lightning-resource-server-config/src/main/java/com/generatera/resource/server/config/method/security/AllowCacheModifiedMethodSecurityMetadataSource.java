package com.generatera.resource.server.config.method.security;

import org.springframework.core.log.LogMessage;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author FLJ
 * @date 2023/2/9
 * @time 16:03
 * @Description 允许手动缓存失效 ...
 */
public class AllowCacheModifiedMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource {
    private static final List<ConfigAttribute> NULL_CONFIG_ATTRIBUTE = Collections.emptyList();
    private final MethodSecurityMetadataSource methodSecurityMetadataSource;
    private volatile Map<AllowCacheModifiedMethodSecurityMetadataSource.DefaultCacheKey, Collection<ConfigAttribute>> attributeCache = new HashMap<>();

    private final Object monitor = new Object();

    public AllowCacheModifiedMethodSecurityMetadataSource(MethodSecurityMetadataSource methodSecurityMetadataSource) {
        Assert.notNull(methodSecurityMetadataSource, "methodSecurityMetadataSource cannot be null");
        this.methodSecurityMetadataSource = methodSecurityMetadataSource;
    }

    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        AllowCacheModifiedMethodSecurityMetadataSource.DefaultCacheKey cacheKey = new AllowCacheModifiedMethodSecurityMetadataSource.DefaultCacheKey(method, targetClass);
        if (attributeCache != null) {
            synchronized (monitor) {
                // 双锁检查 ..
                if(attributeCache != null) {
                    return attributeCache.computeIfAbsent(cacheKey,cache -> getConfigAttributes(method, targetClass, cacheKey));
                }
            }
        }

        return getConfigAttributes(method, targetClass, cacheKey);
    }

    private Collection<ConfigAttribute> getConfigAttributes(Method method, Class<?> targetClass, DefaultCacheKey cacheKey) {

        Collection<ConfigAttribute> attributes =  methodSecurityMetadataSource.getAttributes(method, targetClass);

        if (attributes != null && !attributes.isEmpty()) {
            this.logger.debug(LogMessage.format("Caching method [%s] with attributes %s", cacheKey, attributes));
            this.attributeCache.put(cacheKey, attributes);
            return attributes;
        } else {
            this.attributeCache.put(cacheKey, NULL_CONFIG_ATTRIBUTE);
            return NULL_CONFIG_ATTRIBUTE;
        }
    }

    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return methodSecurityMetadataSource.getAllConfigAttributes();
    }

    public MethodSecurityMetadataSource getMethodSecurityMetadataSource() {
        return this.methodSecurityMetadataSource;
    }

    private static class DefaultCacheKey {
        private final Method method;
        private final Class<?> targetClass;

        DefaultCacheKey(Method method, Class<?> targetClass) {
            this.method = method;
            this.targetClass = targetClass;
        }

        public boolean equals(Object other) {
            AllowCacheModifiedMethodSecurityMetadataSource.DefaultCacheKey otherKey = (AllowCacheModifiedMethodSecurityMetadataSource.DefaultCacheKey) other;
            return this.method.equals(otherKey.method) && ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass);
        }

        public int hashCode() {
            return this.method.hashCode() * 21 + (this.targetClass != null ? this.targetClass.hashCode() : 0);
        }

        public String toString() {
            String targetClassName = this.targetClass != null ? this.targetClass.getName() : "-";
            return "CacheKey[" + targetClassName + "; " + this.method + "]";
        }
    }

    public Map<DefaultCacheKey, Collection<ConfigAttribute>> clearTempAttributeCache() {
        Map<DefaultCacheKey, Collection<ConfigAttribute>> attributeCache = this.attributeCache;
        this.attributeCache = null;
        return Collections.unmodifiableMap(attributeCache);
    }
}
