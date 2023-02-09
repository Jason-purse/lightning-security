package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author FLJ
 * @date 2023/2/7
 * @time 10:50
 * @Description 给与数据库缓存支持 ...
 *
 * // TODO: 2023/2/9  性能缺失,pre/post 一体,如果其中一个修改,那么整体将会被移除 ...
 */
public abstract class ForDataBasedPrePostMethodSecurityMetadataSource extends LightningPrePostMethodSecurityMetadataSource {

    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 能够在 内存压力上来的时候,丢弃一部分内存数据 ...
     * 通过查询库来 实现 一部分牺牲 ...
     * <p>
     * 但是可能会减缓系统的响应速率 ...
     * <p>
     * 按道理来说, 到达软引用状态的引用 仅仅在内存不足够时才进行回收,意味着 gc 不一定进行垃圾回收 ...
     * 而弱引用发生gc则进行回收 ...
     */
    private final Map<OptimizedCacheKey, Collection<ConfigAttribute>> attributeCache = new ConcurrentReferenceHashMap<>();

    private final Map<String, OptimizedCacheKey> attributeKeyCache = new ConcurrentReferenceHashMap<>();

    /**
     * 最开始 进行aop 解析的时候,靠它 ...
     */
    private final Function<Context, Collection<ConfigAttribute>> valueSupplier = context -> {

        if (Object.class == context.targetClass) {
            return Collections.emptyList();
        }

        LightningPreAuthorize preAuthorize = getLightningPreAuthorizeFromClassOrMethod(context.getMethod(), context.targetClass);
        LightningPostAuthorize postAuthorize = getLightningPostAuthorizeFromClassOrMethod(context.method, context.targetClass);

        // 为空的则不处理 ...
        // 也不进行 key 缓存 ...
        if (preAuthorize == null && postAuthorize == null) {
            // 表示没有
            return Collections.emptyList();
        }

        if (preAuthorize != null) {
            OptimizedCacheKey cacheKey = new OptimizedCacheKey(context.method, context.targetClass, preAuthorize.identifier());
            attributeKeyCache.put(cacheKey.getMetadataIdentifier(), context.key);
        }

        if (postAuthorize != null) {
            attributeKeyCache.put(
                    new OptimizedCacheKey(context.method, context.targetClass, postAuthorize.identifier())
                            .getMetadataIdentifier(), context.key);
        }


        return doGetConfigAttribute(context.method, context.targetClass, preAuthorize, postAuthorize);
    };


    public void setNameDiscoverer(DefaultParameterNameDiscoverer nameDiscoverer) {
        this.nameDiscoverer = nameDiscoverer;
    }

    public ForDataBasedPrePostMethodSecurityMetadataSource(
            PrePostInvocationAttributeFactory attributeFactory) {
        super(attributeFactory);
    }

    @Override
    protected PostInvocationAttribute getPostInvocationAttribute(Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        Supplier<String> postInvocationAttributeSupplier = getPostInvocationAttributeSupplier(method, targetClass, postAuthorize);
        String postInvocationAttribute = postInvocationAttributeSupplier.get();

        if (postInvocationAttribute == null) {
            return super.getPostInvocationAttribute(method, targetClass, postAuthorize);
        } else {
            return getAttributeFactory().createPostInvocationAttribute(
                    null, postInvocationAttribute
            );
        }
    }

    @Override
    protected PreInvocationAttribute getPreInvocationAttribute(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        Supplier<String> preInvocationAttributeSupplier = getPreInvocationAttributeSupplier(method, targetClass, preAuthorize);
        String preInvocationAttribute = preInvocationAttributeSupplier.get();
        if (preInvocationAttribute == null) {
            return super.getPreInvocationAttribute(method, targetClass, preAuthorize);
        } else {
            return getAttributeFactory().createPreInvocationAttribute(
                    null, null,
                    preInvocationAttribute
            );
        }
    }


    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        // 先从缓存中获取 ...
        // 如果这样搞,则缓存了两份 ...这是不必要的 ...
        //return super.getAttributes(method, targetClass);
        return attributeCache.computeIfAbsent(new OptimizedCacheKey(method, targetClass), valueSupplier(method, targetClass));

    }

    @NotNull
    private Function<OptimizedCacheKey, Collection<ConfigAttribute>> valueSupplier(Method method, Class<?> targetClass) {
        return cacheKey -> valueSupplier.apply(new Context(cacheKey, method, targetClass));
    }

    private Supplier<String> getPostInvocationAttributeSupplier(Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        return handleRolesAndAuthorities(method, targetClass, postAuthorize.identifier());
    }


    private Supplier<String> getPreInvocationAttributeSupplier(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        return handleRolesAndAuthorities(method, targetClass, preAuthorize.identifier());
    }

    @NotNull
    private Supplier<String> handleRolesAndAuthorities(Method method, Class<?> targetClass, String identifier) {
        return () -> {
            if (StringUtils.hasText(identifier)) {
                ResourceMethodSecurityEntity entity = getResourceMethodSecurityEntity(resolveMethodSecurityIdentifier(
                        method, targetClass, identifier
                ), MethodSecurityInvokePhase.BEFORE.name());

                if (entity != null) {
                    StringBuilder builder = new StringBuilder();
                    LightningPrePostMethodSecurityMetadataSource.handleRolesAndAuthorities(
                            entity.getRoles().split(","),
                            entity.getAuthorities().split(","),
                            builder
                    );
                    String value = builder.toString();
                    return value.length() > 0 ? value : null;
                }
            }
            return null;
        };
    }

    abstract ResourceMethodSecurityEntity getResourceMethodSecurityEntity(
            String methodSecurityIdentifier,
            String invokePhase
    );


    private String resolveMethodSecurityIdentifier(Method method, Class<?> targetClass, String identifier) {
        Parameter[] parameters = method.getParameters();
        return this.resolveMethodSecurityIdentifier(method.getName(), targetClass.getName(), nameDiscoverer.getParameterNames(method),
                Arrays.stream(parameters).map(ele -> ele.getType().getSimpleName()).toArray(String[]::new)
                , identifier);
    }

    private String resolveMethodSecurityIdentifier(String methodName, String className, @Nullable String[] parameterNames, @Nullable String[] parameterTypes, String identifier) {

        // 同时为null,则不判断 ..
        if (parameterNames != null || parameterTypes != null) {
            Assert.isTrue(parameterNames != null && parameterTypes != null && parameterNames.length == parameterTypes.length, "parameter names length must be equals to types !!!");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(className)
                .append("-")
                .append(methodName)
                .append("-");
        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                builder.append(parameterTypes[i])
                        .append("-")
                        .append(parameterNames[i])
                        .append("-");
            }
        }

        builder.append(identifier);
        return builder.toString();
    }


    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        // invoke once
        if (event instanceof ContextRefreshedEvent) {
            // 好像不需要清理 ...
            //但是需要更新数据
            synchronized (attributeKeyCache) {
                updateMetadata(attributeCache);
            }

        } else if (event instanceof MetadataSourceRefreshEvent sources) {
            // 丢弃缓存
            Collection<ResourceMethodSecurityEntity> methodSecurityInfo = sources.getMethodSecurityInfo();
            // 进行缓存清除 ...
            // 此时不允许其他人访问这个缓存信息 ...
            synchronized (attributeCache) {
                for (ResourceMethodSecurityEntity resourceMethodSecurityEntity : methodSecurityInfo) {

                    // TODO: 2023/2/9  将前后放在一起 如果其中一个修改,则引起另一个修改,性能损失 ...

                    OptimizedCacheKey key = attributeKeyCache.remove(resourceMethodSecurityEntity.getMethodSecurityIdentifier());
                    if (key != null) {
                        key.setIdentifier(resourceMethodSecurityEntity.getIdentifier());
                        attributeCache.remove(key);
                        // 恢复 ...
                        key.setIdentifier(null);
                    }
                }
            }

        }
    }


    protected void updateMetadata(Map<OptimizedCacheKey, Collection<ConfigAttribute>> attributeCache) {

        List<ResourceMethodSecurityEntity> values = new LinkedList<>();
        attributeCache.forEach((key, value) -> {

        });
    }

    @AllArgsConstructor
    private static class Context {

        private OptimizedCacheKey key;

        private Method method;

        private Class<?> targetClass;


        public Class<?> getTargetClass() {
            return targetClass;
        }

        public OptimizedCacheKey getKey() {
            return key;
        }

        public Method getMethod() {
            return method;
        }

    }

    private class OptimizedCacheKey extends DefaultCacheKey {

        @Nullable
        private String identifier;

        OptimizedCacheKey(Method method, Class<?> targetClass) {
            super(method, targetClass);
        }

        OptimizedCacheKey(Method method, Class<?> targetClass, @NotNull String identifier) {
            super(method, targetClass);
            this.identifier = identifier;
        }


        public void setIdentifier(@Nullable String identifier) {
            this.identifier = identifier;
        }

        @Nullable
        public String getIdentifier() {
            return identifier;
        }

        public String getMetadataIdentifier() {
            return ForDataBasedPrePostMethodSecurityMetadataSource.this.resolveMethodSecurityIdentifier(
                    getMethod(), getTargetClass(), identifier
            );
        }

        @Override
        public int hashCode() {
            return identifier != null ? identifier.hashCode() * 21 + super.hashCode() : super.hashCode();
        }
    }
}
