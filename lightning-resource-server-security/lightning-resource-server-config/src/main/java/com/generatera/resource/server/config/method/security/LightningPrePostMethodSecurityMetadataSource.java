package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.util.LightningInvocationAttributeUtils;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.boot.starter.util.dataflow.Tuple4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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


    protected List<ConfigAttribute> getConfigAttributes(Method method, Class<?> targetClass) {

        // 支持类上面的针对全局所有方法进行处理 ...
        if (targetClass == Object.class) {
            return Collections.emptyList();
        }

        // 底层做缓存了,无需担心 ..
        List<ConfigAttribute> configAttributes = doGetConfigAttribute(method, targetClass,
                getLightningPreAuthorizeFromClassOrMethod(method, targetClass),
                getLightningPostAuthorizeFromClassOrMethod(method, targetClass));

        LightningInvocationAttributeUtils.evaluateAndSetPostInvocationResourceMethodSecurity(configAttributes);
        LightningInvocationAttributeUtils.evaluateAndSetPostInvocationResourceMethodSecurity(configAttributes);

        if (configAttributes != null && !configAttributes.isEmpty()) {
            return unWrapToNativeConfigAttribute(configAttributes);
        }

        return configAttributes;
    }

    private List<ConfigAttribute> unWrapToNativeConfigAttribute(Collection<ConfigAttribute> configAttributes) {
        return configAttributes.stream().map(ele -> {
            if (ele instanceof LightningInvocationAttribute attribute) {
                return attribute.getDelegate();
            }
            return ele;
        }).toList();
    }

    public String resolveMethodSecurityIdentifier(Method method, Class<?> targetClass) {
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
        String methodSecurityIdentifier = resolveMethodSecurityIdentifier(method, targetClass);
        if (postAuthorize != null) {
            handleRolesAndAuthorities(postAuthorize.roles(), postAuthorize.authorities(),
                    postAuthorize.authorizeMode(),
                    methodSecurityIdentifier,
                    builder);
        }
        String postAuthorizeAttribute = builder.toString();
        PostInvocationAttribute postInvocationAttribute = this.attributeFactory.createPostInvocationAttribute(null, postAuthorizeAttribute.length() > 0 ? postAuthorizeAttribute : null);
        if (postInvocationAttribute != null) {
            if (postAuthorize != null) {
                return new LightningPostInvocationAttribute(postInvocationAttribute, methodSecurityIdentifier,
                        resolveResourceBehavior(postAuthorize.behavior(), method, targetClass));
            }
        }
        return postInvocationAttribute;
    }

    protected PreInvocationAttribute getPreInvocationAttribute(Method method, Class<?> targetClass, LightningPreAuthorize preAuthorize) {
        StringBuilder builder = new StringBuilder();
        String securityIdentifier = resolveMethodSecurityIdentifier(method, targetClass);

        if (preAuthorize != null) {
            handleRolesAndAuthorities(preAuthorize.roles(), preAuthorize.authorities(), preAuthorize.authorizeMode(),
                    securityIdentifier, builder);
        }

        String preAuthorizeAttribute = builder.toString();
        PreInvocationAttribute preInvocationAttribute = this.attributeFactory.createPreInvocationAttribute(null, null, preAuthorizeAttribute.length() > 0 ? preAuthorizeAttribute : null);
        if (preInvocationAttribute != null) {
            if (preAuthorize != null) {
                return new LightningPreInvocationAttribute(preInvocationAttribute, securityIdentifier,
                        resolveResourceBehavior(preAuthorize.behavior(), method, targetClass));
            }
        }
        return null;
    }

    protected String resolveResourceBehavior(String behaviorStr, Method method, Class<?> targetClass) {
        return OptionalFlux.string(behaviorStr)
                .switchMap(
                        behavior -> {
                            // 进行解析,是否可用 ..
                            if (!ResourceBehavior.getBehaviors().contains(behavior)) {
                                return determineResourceBehavior(method, targetClass);
                            }
                            return behavior;
                        },
                        () -> determineResourceBehavior(method, targetClass)
                )
                .getResult();
    }

    /**
     * 检测行为 ...
     */
    protected String determineResourceBehavior(Method method, Class<?> target) {

        RequestMapping annotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(target, RequestMapping.class);
        }
        Assert.notNull(annotation, "request mapping for method security must not be null");
        RequestMethod[] requestMethods = annotation.method();
        Set<String> behavior = new LinkedHashSet<>();
        for (RequestMethod requestMethod : requestMethods) {
            if (requestMethod.equals(RequestMethod.GET) || requestMethod.equals(RequestMethod.HEAD)) {
                behavior.add(ResourceBehavior.READ);
            } else {
                behavior.add(ResourceBehavior.WRITE);
            }
        }

        // 大于1个的时候,其实是没法处理的 ...
        // 也就是这里报错 ...
        if (behavior.size() > 1) {
            boolean existsRead = false;
            boolean existsWrite = false;
            for (String s : behavior) {
                if (ResourceBehavior.READ.equals(s)) {
                    existsRead = true;
                    break;
                }
                if (ResourceBehavior.WRITE.equals(s)) {
                    existsWrite = true;
                }
            }

            if (existsRead && existsWrite) {
                behavior.remove(ResourceBehavior.READ);
                behavior.remove(ResourceBehavior.WRITE);

                // 又读又写
                behavior.add(ResourceBehavior.WRITE_AND_READ);
            }
        }

        Assert.isTrue(behavior.size() == 1, "The current resource behavior exceeds one ,resource behavior must be unique !!!");

        return behavior.iterator().next();
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
        if(event instanceof AuthorizedEvent authorizedEvent) {
            // 尝试设置 ..
            Collection<ConfigAttribute> configAttributes = authorizedEvent.getConfigAttributes();

            for (ConfigAttribute configAttribute : configAttributes) {

            }
        }
    }

    private static class LightningPreInvocationAttribute implements LightningInvocationAttribute, PreInvocationAttribute {
        private final PreInvocationAttribute preInvocationAttribute;
        private final Tuple4<String, String, String, String> methodIdentifierWithInfo;

        public LightningPreInvocationAttribute(@NotNull PreInvocationAttribute preInvocationAttribute, String methodIdentifier, String behavior) {
            this.preInvocationAttribute = preInvocationAttribute;
            this.methodIdentifierWithInfo = new Tuple4<>(methodIdentifier, ResourceType.BACKEND_TYPE.getType(), MethodSecurityInvokePhase.BEFORE.name(), behavior);
        }

        @Override
        public Tuple4<String, String, String, String> getMethodIdentifierWithActionAndType() {
            return methodIdentifierWithInfo;
        }

        @Override
        public String getAttribute() {
            return preInvocationAttribute.getAttribute();
        }

        @Override
        public ConfigAttribute getDelegate() {
            return preInvocationAttribute;
        }
    }

    private static class LightningPostInvocationAttribute implements LightningInvocationAttribute, PostInvocationAttribute {
        private final PostInvocationAttribute preInvocationAttribute;
        private final Tuple4<String, String, String, String> methodIdentifierWithInfo;

        public LightningPostInvocationAttribute(@NotNull PostInvocationAttribute preInvocationAttribute, String methodIdentifier, String behavior) {
            this.preInvocationAttribute = preInvocationAttribute;
            this.methodIdentifierWithInfo = new Tuple4<>(methodIdentifier, ResourceType.BACKEND_TYPE.getType(), MethodSecurityInvokePhase.AFTER.name(), behavior);
        }

        @Override
        public Tuple4<String, String, String, String> getMethodIdentifierWithActionAndType() {
            return methodIdentifierWithInfo;
        }

        @Override
        public ConfigAttribute getDelegate() {
            return preInvocationAttribute;
        }

        @Override
        public String getAttribute() {
            return preInvocationAttribute.getAttribute();
        }
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

        @Override
        public boolean equals(Object other) {
            if (other instanceof DefaultCacheKey otherKey) {
                return other == this || this.method.equals(otherKey.method) && ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.method.hashCode() * 21 + (this.targetClass != null ? this.targetClass.hashCode() : 0);
        }

        @Override
        public String toString() {
            String targetClassName = this.targetClass != null ? this.targetClass.getName() : "-";
            return "CacheKey[" + targetClassName + "; " + this.method + "]";
        }

    }
}
