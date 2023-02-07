package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author FLJ
 * @date 2023/2/7
 * @time 10:50
 * @Description 给与数据库缓存支持 ...
 */
public abstract class ForDataBasedPrePostMethodSecurityMetadataSource extends LightningPrePostMethodSecurityMetadataSource {

    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();


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




    private Supplier<String> getPostInvocationAttributeSupplier(Method method, Class<?> targetClass, LightningPostAuthorize postAuthorize) {
        return handleRolesAndAuthorities(method,targetClass,postAuthorize.identifier());
    }

    private String resolveMethodSecurityIdentifier(Method method, Class<?> targetClass, String identifier) {
        StringBuilder builder = new StringBuilder();
        builder.append(targetClass.getName())
                .append("-")
                .append(method.getName())
                .append("-");
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        if (parameterNames != null && parameterNames.length > 0) {
            List<String> parameterTypes = new LinkedList<>();
            for (Parameter parameter : parameters) {
                parameterTypes.add(parameter.getType().getSimpleName());
            }
            for (int i = 0; i < parameterNames.length; i++) {
                builder.append(parameterTypes.get(i))
                        .append("-")
                        .append(parameterNames[i])
                        .append("-");
            }
        }

        builder.append(identifier);
        return builder.toString();
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
}
