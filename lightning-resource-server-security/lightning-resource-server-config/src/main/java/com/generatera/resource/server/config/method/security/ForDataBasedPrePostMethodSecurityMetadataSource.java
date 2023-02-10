package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.method.security.entity.ResourceMethodSecurityEntity;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
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
 * <p>
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
            PrePostInvocationAttributeFactory attributeFactory,String moduleName) {
        super(attributeFactory,moduleName);
    }

    @Override
    protected PostInvocationAttribute getPostInvocationAttribute(Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        Supplier<ConfigAttribute> postInvocationAttributeSupplier = getPostInvocationAttributeSupplier(method, targetClass, postAuthorize);
        return ((PostInvocationAttribute) postInvocationAttributeSupplier.get());
    }

    @Override
    protected PreInvocationAttribute getPreInvocationAttribute(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        Supplier<ConfigAttribute> preInvocationAttributeSupplier = getPreInvocationAttributeSupplier(method, targetClass, preAuthorize);
        return (PreInvocationAttribute) preInvocationAttributeSupplier.get();
    }

    protected PreInvocationAttribute getPreInvocationAttribute0(String preInvocationAttribute, Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        if (!StringUtils.hasText(preInvocationAttribute)) {
            return super.getPreInvocationAttribute(method, targetClass, preAuthorize);
        } else {
            return getAttributeFactory().createPreInvocationAttribute(
                    null, null,
                    preInvocationAttribute
            );
        }
    }

    protected PostInvocationAttribute getPostInvocationAttribute0(String postInvocationAttribute, Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        if (postInvocationAttribute == null) {
            return super.getPostInvocationAttribute(method, targetClass, postAuthorize);
        } else {
            return getAttributeFactory().createPostInvocationAttribute(
                    null, postInvocationAttribute
            );
        }
    }


    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        // 先从缓存中获取 ...
        // 如果这样搞,则缓存了两份 ...这是不必要的 ...
        //return super.getAttributes(method, targetClass);
        Collection<ConfigAttribute> configAttributes = attributeCache.computeIfAbsent(new OptimizedCacheKey(method, targetClass), valueSupplier(method, targetClass));
        if (configAttributes != null && !configAttributes.isEmpty()) {
            return unWrapToNativeConfigAttribute(configAttributes);
        }
        return configAttributes;
    }

    private List<ConfigAttribute> unWrapToNativeConfigAttribute(Collection<ConfigAttribute> configAttributes) {
        return configAttributes.stream().map(ele -> {
            if (ele instanceof AnnotationInfoWithPostConfigAttribute attribute) {
                return attribute.delegate;
            } else if (ele instanceof AnnotationInfoWithPreConfigAttribute attribute) {
                return attribute.delegate;
            }
            return ele;
        }).toList();
    }

    @NotNull
    private Function<OptimizedCacheKey, Collection<ConfigAttribute>> valueSupplier(Method method, Class<?> targetClass) {
        return cacheKey -> valueSupplier.apply(new Context(cacheKey, method, targetClass));
    }

    private Supplier<ConfigAttribute> getPostInvocationAttributeSupplier(Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        return handleRolesAndAuthorities(method, targetClass, postAuthorize, MethodSecurityInvokePhase.AFTER);
    }


    private Supplier<ConfigAttribute> getPreInvocationAttributeSupplier(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        return handleRolesAndAuthorities(method, targetClass, preAuthorize, MethodSecurityInvokePhase.BEFORE);
    }

    @NotNull
    private Supplier<ConfigAttribute> handleRolesAndAuthorities(Method method, Class<?> targetClass, Annotation annotation, MethodSecurityInvokePhase phase) {
        return () -> {
            ResourceMethodSecurityEntity entity = getResourceMethodSecurityEntity(
                    resolveMethodSecurityIdentifier(method, targetClass), phase.name()
            );
            String value = null;
            if (entity != null) {
                StringBuilder builder = new StringBuilder();
                LightningPrePostMethodSecurityMetadataSource.handleRolesAndAuthorities(
                        entity.getRoles().split(","),
                        entity.getAuthorities().split(","),
                        builder
                );
                value = builder.toString();
            }
            if (phase == MethodSecurityInvokePhase.BEFORE) {
                LightningPreAuthorize preAuthorize = (LightningPreAuthorize) annotation;
                PreInvocationAttribute preInvocationAttribute0 = getPreInvocationAttribute0(value, method, targetClass, preAuthorize);
                return new AnnotationInfoWithPreConfigAttribute(
                        preInvocationAttribute0,
                        method,
                        targetClass,
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getRoles)
                                .map(this::resolveToList)
                                .orElse(resolveToList(preAuthorize.roles())).getResult(),
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getAuthorities)
                                .map(this::resolveToList)
                                .orElse(resolveToList(preAuthorize.authorities()))
                                .getResult(),
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getIdentifier)
                                .orElse(ElvisUtil.stringElvis(preAuthorize.identifier(), LightningPreAuthorize.DEFAULT_IDENTIFIER)).getResult(),
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getDescription)
                                .orElse(preAuthorize.description()).getResult()
                );
            } else if (phase == MethodSecurityInvokePhase.AFTER) {
                LightningPostAuthorize postAuthorize = (LightningPostAuthorize) annotation;
                PostInvocationAttribute postInvocationAttribute0 = getPostInvocationAttribute0(value, method, targetClass, postAuthorize);
                return new AnnotationInfoWithPostConfigAttribute(
                        postInvocationAttribute0,
                        method,
                        targetClass,
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getRoles)
                                .map(this::resolveToList)
                                .orElse(resolveToList(postAuthorize.roles())).getResult(),
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getAuthorities)
                                .map(this::resolveToList)
                                .orElse(resolveToList(postAuthorize.authorities()))
                                .getResult(),
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getIdentifier)
                                .orElse(ElvisUtil.stringElvis(postAuthorize.identifier(), LightningPostAuthorize.DEFAULT_IDENTIFIER)).getResult(),
                        OptionalFlux.of(entity)
                                .map(ResourceMethodSecurityEntity::getDescription)
                                .orElse(postAuthorize.description()).getResult()
                );
            }

            return null;
        };
    }

    private List<String> resolveToList(String value) {
        if (StringUtils.hasText(value)) {
            return List.of(value.split(","));
        }
        return null;
    }

    private List<String> resolveToList(String[] values) {
        if (values != null && values.length > 0) {
            return List.of(values);
        }
        return null;
    }

    private String toString(List<String> values) {
        if (values != null) {
            return org.apache.commons.lang3.StringUtils.join(
                    values, ','
            );
        }
        return "";
    }

    abstract ResourceMethodSecurityEntity getResourceMethodSecurityEntity(
            String methodSecurityIdentifier,
            String invokePhase
    );


    private String resolveMethodSecurityIdentifier(Method method, Class<?> targetClass) {
        Parameter[] parameters = method.getParameters();
        return this.resolveMethodSecurityIdentifier(method.getName(), targetClass.getName(),
                OptionalFlux.of(nameDiscoverer.getParameterNames(method)).orElse(resolveParameterNames(method)).getResult(),
                Arrays.stream(parameters).map(ele -> ele.getType().getSimpleName()).toArray(String[]::new));
    }

    private String[] resolveParameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] values = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            values[i] = parameters[i].getName();
        }
        return values;
    }

    private String resolveMethodSecurityIdentifier(String methodName, String className, @Nullable String[] parameterNames, @Nullable String[] parameterTypes) {

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

        return builder.toString();
    }


    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        // invoke once
        if (event instanceof ContextRefreshedEvent) {
            // 好像不需要清理 ...
            //但是需要更新数据
            synchronized (attributeCache) {
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
        } else if (event instanceof MetadataSourceAllRefreshEvent) {
            synchronized (attributeCache) {
                attributeKeyCache.clear();
                attributeCache.clear();
            }
        }
    }


    protected void updateMetadata(Map<OptimizedCacheKey, Collection<ConfigAttribute>> attributeCache) {

        List<ResourceMethodSecurityEntity> values = new LinkedList<>();
        attributeCache.forEach((key, value) -> {
            List<String> preAuthorities = new LinkedList<>();
            List<String> preRoles = new LinkedList<>();
            List<String> postAuthorities = new LinkedList<>();
            List<String> postRoles = new LinkedList<>();

            String preIdentifier = null;

            String postIdentifier = null;
            String preDescription = null;
            String postDescription = null;

            // 本质上只有两个 ..
            // 可以优化代码 ..
            for (ConfigAttribute configAttribute : value) {
                if (configAttribute instanceof PostInvocationAttribute) {
                    AnnotationInfoWithPostConfigAttribute info = (AnnotationInfoWithPostConfigAttribute) configAttribute;
                    if (!CollectionUtils.isEmpty(info.roles)) {
                        postRoles.addAll(info.roles);
                    }
                    if (!CollectionUtils.isEmpty(info.authorities)) {
                        postAuthorities.addAll(info.authorities);
                    }
                    if (postDescription == null) {
                        postDescription = info.description;
                    }
                    if (postIdentifier == null) {
                        postIdentifier = info.identifier;
                    }

                } else {
                    AnnotationInfoWithPreConfigAttribute info = (AnnotationInfoWithPreConfigAttribute) configAttribute;
                    if (!CollectionUtils.isEmpty(info.roles)) {
                        preRoles.addAll(info.roles);
                    }
                    if (!CollectionUtils.isEmpty(info.authorities)) {
                        preAuthorities.addAll(info.authorities);
                    }
                    if (preDescription == null) {
                        preDescription = info.description;
                    }
                    if (preIdentifier == null) {
                        preIdentifier = info.identifier;
                    }
                }

            }

            if (!preAuthorities.isEmpty() || !preRoles.isEmpty()) {
                values.add(
                        ResourceMethodSecurityEntity.builder()
                                .invokePhase(MethodSecurityInvokePhase.BEFORE.name())
                                .methodName(key.getMethod().getName())
                                .methodSecurityIdentifier(resolveMethodSecurityIdentifier(key.getMethod(), key.getTargetClass()))
                                .authorities(toString(preAuthorities))
                                .description(preDescription)
                                .roles(toString(preRoles))
                                .identifier(preIdentifier)
                                .moduleName(moduleName)
                                .build()
                );
            }

            if (!postAuthorities.isEmpty() || !postRoles.isEmpty()) {
                values.add(
                        ResourceMethodSecurityEntity.builder()
                                .invokePhase(MethodSecurityInvokePhase.AFTER.name())
                                .methodName(key.getMethod().getName())
                                .methodSecurityIdentifier(
                                        resolveMethodSecurityIdentifier(key.getMethod(),
                                                key.getTargetClass())
                                )
                                .roles(toString(postRoles))
                                .authorities(toString(postAuthorities))
                                .description(postDescription)
                                .identifier(postIdentifier)
                                .moduleName(moduleName)
                                .build()
                );
            }


        });

        if (!CollectionUtils.isEmpty(values)) {
            updateResourceMethodSecurityMetadata(values);
        }
    }

    protected abstract void updateResourceMethodSecurityMetadata(List<ResourceMethodSecurityEntity> entities);

    @Override
    public boolean isCacheable() {
        return false;
    }

    private static class AnnotationInfoWithPreConfigAttribute implements PreInvocationAttribute {

        private final ConfigAttribute delegate;

        public Method method;

        public Class<?> targetClass;

        public List<String> roles;

        public List<String> authorities;

        public String identifier;

        public String description;


        public AnnotationInfoWithPreConfigAttribute(ConfigAttribute delegate,
                                                    Method method, Class<?> targetClass,
                                                    List<String> roles,
                                                    List<String> authorities,
                                                    String identifier,
                                                    String description) {
            this.delegate = delegate;
            this.method = method;
            this.targetClass = targetClass;
            this.roles = roles;
            this.authorities = authorities;
            this.identifier = identifier;
            this.description = description;
        }

        @Override
        public String getAttribute() {
            return delegate.getAttribute();
        }


    }

    private static class AnnotationInfoWithPostConfigAttribute implements PostInvocationAttribute {

        private final ConfigAttribute delegate;

        public Method method;

        public Class<?> targetClass;

        public List<String> roles;

        public List<String> authorities;

        public String identifier;

        public String description;

        public AnnotationInfoWithPostConfigAttribute(ConfigAttribute delegate,
                                                     Method method, Class<?> targetClass,
                                                     List<String> roles,
                                                     List<String> authorities,
                                                     String identifier,
                                                     String description) {
            this.delegate = delegate;
            this.method = method;
            this.targetClass = targetClass;
            this.roles = roles;
            this.authorities = authorities;
            this.identifier = identifier;
            this.description = description;
        }

        @Override
        public String getAttribute() {
            return delegate.getAttribute();
        }

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
                    getMethod(), getTargetClass()
            );
        }

        @Override
        public int hashCode() {
            return identifier != null ? identifier.hashCode() * 21 + super.hashCode() : super.hashCode();
        }
    }
}
