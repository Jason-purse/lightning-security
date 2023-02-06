package com.generatera.resource.server.config;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 13:48
 * @Description 支持 {@link LightningPreAuthorize} 以及{@link LightningPostAuthorize}等注解的处理 ..
 */
public class LightningPrePostMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource implements LightningExtMethodSecurityMetadataSource {

    private final PrePostInvocationAttributeFactory attributeFactory;


    public LightningPrePostMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory) {
        this.attributeFactory = attributeFactory;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        LightningPreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, LightningPreAuthorize.class);
        LightningPostAuthorize postAuthorize = AnnotationUtils.findAnnotation(method, LightningPostAuthorize.class);
        if (preAuthorize == null && postAuthorize == null) {
            return Collections.emptyList();
        } else {

            StringBuilder builder = new StringBuilder();
            if (preAuthorize != null) {
                handleRolesAndAuthorities(preAuthorize.roles(), preAuthorize.authorities(), builder);
            }

            ArrayList<ConfigAttribute> attrs = new ArrayList<>(2);

            String preAuthorizeAttribute = builder.toString();
            PreInvocationAttribute pre = this.attributeFactory.createPreInvocationAttribute(null, null, preAuthorizeAttribute.length() > 0 ? preAuthorizeAttribute : null);
            if (pre != null) {
                attrs.add(pre);
            }
            builder = new StringBuilder();
            if(postAuthorize != null) {
                handleRolesAndAuthorities(postAuthorize.roles(), postAuthorize.authorities(), builder);
            }

            String postAuthorizeAttribute = builder.toString();
            PostInvocationAttribute post = this.attributeFactory.createPostInvocationAttribute(null, postAuthorizeAttribute.length() > 0 ? postAuthorizeAttribute : null);
            if (post != null) {
                attrs.add(post);
            }

            attrs.trimToSize();
            return attrs;
        }
    }

     static void handleRolesAndAuthorities(String[] roles,String[] authorities, StringBuilder builder) {
        if (!ObjectUtils.isEmpty(roles)) {
            handleRoles(roles, builder);
        }

        if (!ObjectUtils.isEmpty(authorities)) {
            builder.append(" or ");
            handleAuthorities(authorities, builder);
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
        configs.replace(configs.length() - 1,configs.length(),"");
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
        configs.replace(configs.length() - 1,configs.length(),"");
        configs.append(" )");
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }
}
