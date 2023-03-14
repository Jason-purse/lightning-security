package com.generatera.resource.server.config.method.security;

import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 13:48
 * @Description 支持 {@link LightningPreAuthorize} 以及{@link LightningPostAuthorize}等注解的处理 ..
 */
public class LightningPrePostMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource implements LightningExtMethodSecurityMetadataSource {

    private final PrePostInvocationAttributeFactory attributeFactory;

    protected final String moduleName;

    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();


    public LightningPrePostMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory, String moduleName) {
        this.attributeFactory = attributeFactory;
        Assert.hasText(moduleName, "moduleName must not be null !!!");
        this.moduleName = moduleName;
    }

    public void setNameDiscoverer(DefaultParameterNameDiscoverer nameDiscoverer) {
        this.nameDiscoverer = nameDiscoverer;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        return getConfigAttributes(method, targetClass);
    }

    @NotNull
    protected List<ConfigAttribute> getConfigAttributes(Method method, Class<?> targetClass) {

        // 支持类上面的针对全局所有方法进行处理 ...
        if (targetClass == Object.class) {
            return Collections.emptyList();
        }

        // 底层做缓存了,无需担心 ..
        return doGetConfigAttribute(method, targetClass,
                getLightningPreAuthorizeFromClassOrMethod(method, targetClass),
                getLightningPostAuthorizeFromClassOrMethod(method, targetClass));
    }

    protected String resolveMethodSecurityIdentifier(Method method, Class<?> targetClass) {
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

    protected List<ConfigAttribute> doGetConfigAttribute(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize, LightningPostAuthorize postAuthorize) {

        if (preAuthorize == null && postAuthorize == null) {
            return Collections.emptyList();
        } else {
            ArrayList<ConfigAttribute> attrs = new ArrayList<>(2);

            if (preAuthorize != null) {
                PreInvocationAttribute pre = getPreInvocationAttribute(method, targetClass, preAuthorize);
                if (pre != null) {
                    attrs.add(pre);
                }
            }

            if (postAuthorize != null) {
                PostInvocationAttribute post = getPostInvocationAttribute(method, targetClass, postAuthorize);
                if (post != null) {
                    attrs.add(post);
                }

            }
            attrs.trimToSize();
            return attrs;
        }
    }

    @Nullable
    protected LightningPostAuthorize getLightningPostAuthorizeFromClassOrMethod(Method method, Class<?> targetClass) {
        LightningPostAuthorize postAuthorize = AnnotationUtils.findAnnotation(method, LightningPostAuthorize.class);

        if (postAuthorize == null) {
            postAuthorize = AnnotationUtils.findAnnotation(targetClass, LightningPostAuthorize.class);
        }
        return postAuthorize;
    }

    @Nullable
    protected LightningPreAuthorize getLightningPreAuthorizeFromClassOrMethod(Method method, Class<?> targetClass) {
        LightningPreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, LightningPreAuthorize.class);

        if (preAuthorize == null) {
            // 尝试从类上获取
            preAuthorize = AnnotationUtils.findAnnotation(targetClass, LightningPreAuthorize.class);
        }
        return preAuthorize;
    }

    protected PostInvocationAttribute getPostInvocationAttribute(Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        StringBuilder builder = new StringBuilder();
        if (postAuthorize != null) {
            handleRolesAndAuthorities(postAuthorize.roles(), postAuthorize.authorities(),
                    postAuthorize.authorizeMode(),
                    resolveMethodSecurityIdentifier(method, targetClass),
                    builder);
        }
        String postAuthorizeAttribute = builder.toString();
        return this.attributeFactory.createPostInvocationAttribute(null, postAuthorizeAttribute.length() > 0 ? postAuthorizeAttribute : null);
    }

    protected PreInvocationAttribute getPreInvocationAttribute(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        StringBuilder builder = new StringBuilder();
        if (preAuthorize != null) {
            handleRolesAndAuthorities(preAuthorize.roles(), preAuthorize.authorities(), preAuthorize.authorizeMode(),
                    resolveMethodSecurityIdentifier(method, targetClass), builder);
        }

        String preAuthorizeAttribute = builder.toString();
        return this.attributeFactory.createPreInvocationAttribute(null, null, preAuthorizeAttribute.length() > 0 ? preAuthorizeAttribute : null);
    }

    static void handleRolesAndAuthorities(String[] roles, String[] authorities, AuthorizeMode authorizeMode, String methodIdentifier, StringBuilder builder) {
        if (authorizeMode == AuthorizeMode.AUTHORITIES_TO_ROLE) {
            if (!ObjectUtils.isEmpty(roles)) {
                handleRoles(roles, builder);
            }

            if (!ObjectUtils.isEmpty(authorities)) {
                builder.append(" or ");
                handleAuthorities(authorities, builder);
            }
        } else {
            handleAuthorities(new String[]{methodIdentifier}, builder);
        }
    }


    static void handleRoles(String[] roles, StringBuilder configs) {
        configs.append("hasAnyRole(");
        for (String role : roles) {
            configs.append("'");
            configs.append(role);
            configs.append("'");
            configs.append(",");
        }
        configs.replace(configs.length() - 1, configs.length(), "");
        configs.append(")");
    }

    static void handleAuthorities(String[] authorities, StringBuilder configs) {
        configs.append("hasAnyAuthority( ");

        for (String authority : authorities) {
            configs.append("'");
            configs.append(authority);
            configs.append("'");
            configs.append(",");
        }
        configs.replace(configs.length() - 1, configs.length(), "");
        configs.append(" )");
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    protected PrePostInvocationAttributeFactory getAttributeFactory() {
        return attributeFactory;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        // 啥也不做 ...
    }


    protected static class DefaultCacheKey {
        private final Method method;
        private final Class<?> targetClass;

        DefaultCacheKey(Method method, Class<?> targetClass) {
            this.method = method;
            this.targetClass = targetClass;
        }


        public Method getMethod() {
            return method;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public boolean equals(Object other) {
            if (other instanceof DefaultCacheKey otherKey) {
                return other == this || this.method.equals(otherKey.method) && ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass);
            }
            return false;
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
