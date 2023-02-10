package com.generatera.resource.server.config.method.security;

import org.springframework.context.ApplicationEvent;
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
 * @Description 条件部分缓存数据 ...
 * <p>
 * 基于选择条件 实现 部分数据缓存 ...
 * <p>
 * 根据条件实现部分缓存 ... 如果{@code MethodSecurityMetadataSource} 是一个 {@link  LightningExtMethodSecurityMetadataSource}
 * 则自动支持 可缓存能力检测, 否则自动是缓存的 ...
 */
public class AllowPartialCacheMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource implements LightningExtMethodSecurityMetadataSource {
    private static final List<ConfigAttribute> NULL_CONFIG_ATTRIBUTE = Collections.emptyList();
    private final List<MethodSecurityMetadataSource> methodSecurityMetadataSources;
    private final Map<DefaultCacheKey, Collection<ConfigAttribute>> attributeCache = new HashMap<>();

    private final Map<MethodSecurityMetadataSource, Boolean> cacheFlags = new HashMap<>();

    public AllowPartialCacheMethodSecurityMetadataSource(MethodSecurityMetadataSource... methodSecurityMetadataSources) {
        this(List.of(methodSecurityMetadataSources));
    }

    public AllowPartialCacheMethodSecurityMetadataSource(List<MethodSecurityMetadataSource> methodSecurityMetadataSources) {
        Assert.notNull(methodSecurityMetadataSources, "methodSecurityMetadataSources cannot be null");
        this.methodSecurityMetadataSources = methodSecurityMetadataSources;
        for (MethodSecurityMetadataSource methodSecurityMetadataSource : methodSecurityMetadataSources) {
            if (methodSecurityMetadataSource instanceof LightningExtMethodSecurityMetadataSource extMethodSecurityMetadataSource) {
                cacheFlags.put(methodSecurityMetadataSource, extMethodSecurityMetadataSource.isCacheable());
            } else {
                cacheFlags.put(methodSecurityMetadataSource, true);
            }
        }
    }

    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        DefaultCacheKey cacheKey = new DefaultCacheKey(method, targetClass);
        return getConfigAttributes(method, targetClass, cacheKey);
    }

    private Collection<ConfigAttribute> getConfigAttributes(Method method, Class<?> targetClass, DefaultCacheKey cacheKey) {
        Collection<ConfigAttribute> attributes = null;

        // 性能损耗 ..
        for (MethodSecurityMetadataSource s : this.methodSecurityMetadataSources) {
            Boolean aBoolean = cacheFlags.get(s);
            if (aBoolean) {
                return getCachedConfigAttributes(cacheKey, s);
            }

            // 尝试 获取
            attributes = s.getAttributes(method, targetClass);
            if (attributes != null && !attributes.isEmpty()) {
                break;
            }
        }

        if (attributes != null && !attributes.isEmpty()) {
            this.logger.debug(LogMessage.format("Caching method [%s] with attributes %s", cacheKey, attributes));
            this.attributeCache.put(cacheKey, attributes);
            return attributes;
        } else {
            this.attributeCache.put(cacheKey, NULL_CONFIG_ATTRIBUTE);
            return NULL_CONFIG_ATTRIBUTE;
        }
    }

    private Collection<ConfigAttribute> getCachedConfigAttributes(DefaultCacheKey cacheKey, MethodSecurityMetadataSource source) {
        synchronized (this.attributeCache) {
            Collection<ConfigAttribute> cached = this.attributeCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            Collection<ConfigAttribute> attributes = source.getAttributes(cacheKey.method, cacheKey.targetClass);
            Collection<ConfigAttribute> value = attributes != null && !attributes.isEmpty() ? attributes : NULL_CONFIG_ATTRIBUTE;
            // 设置缓存 ..
            this.attributeCache.put(cacheKey, value);
            return value;
        }
    }

    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> set = new HashSet<>();

        for (MethodSecurityMetadataSource s : this.methodSecurityMetadataSources) {
            Collection<ConfigAttribute> attrs = s.getAllConfigAttributes();
            if (attrs != null) {
                set.addAll(attrs);
            }
        }

        return set;
    }

    public List<MethodSecurityMetadataSource> getMethodSecurityMetadataSources() {
        return this.methodSecurityMetadataSources;
    }

    /**
     * 对于不可缓存的  metadata source,可以 进行事件接收 ...
     */
    public void onApplicationEvent(ApplicationEvent event) {
        cacheFlags.forEach((key, flag) -> {
            // 不可缓存 ...
            if (!flag) {
                LightningExtMethodSecurityMetadataSource source = (LightningExtMethodSecurityMetadataSource) key;
                source.onApplicationEvent(event);
            }
        });
    }

    private static class DefaultCacheKey {
        private final Method method;
        private final Class<?> targetClass;

        DefaultCacheKey(Method method, Class<?> targetClass) {
            this.method = method;
            this.targetClass = targetClass;
        }

        public boolean equals(Object other) {
            AllowPartialCacheMethodSecurityMetadataSource.DefaultCacheKey otherKey = (AllowPartialCacheMethodSecurityMetadataSource.DefaultCacheKey) other;
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
}
