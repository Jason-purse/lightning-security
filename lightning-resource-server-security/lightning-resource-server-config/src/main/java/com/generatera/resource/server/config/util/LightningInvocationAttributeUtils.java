package com.generatera.resource.server.config.util;

import com.generatera.resource.server.config.LightningResourceMethodSecurityHolder;
import com.generatera.resource.server.config.method.security.LightningInvocationAttribute;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;

import java.util.Collection;

/**
 * .class
 *
 * @author tianzhangxu
 * @date 2023/4/6 18:47
 */
public class LightningInvocationAttributeUtils {

    public static void evaluateAndSetPreInvocationResourceMethodSecurity(Collection<ConfigAttribute> attributes) {
        if(attributes != null) {
            for (ConfigAttribute attribute : attributes) {
                if (attribute instanceof LightningInvocationAttribute preInvocationAttribute) {
                    if (preInvocationAttribute instanceof PreInvocationAttribute) {
                        // 设置当前资源方法安全holder
                        LightningResourceMethodSecurityHolder.setPreResourceMethodSecurity(preInvocationAttribute.getMethodIdentifierWithActionAndType());
                    }
                }
            }
        }
    }

    public static void evaluateAndSetPostInvocationResourceMethodSecurity(Collection<ConfigAttribute> attributes) {
        if(attributes != null) {
            for (ConfigAttribute attribute : attributes) {
                if (attribute instanceof LightningInvocationAttribute invocationAttribute) {
                    if (attribute instanceof PostInvocationAttribute) {
                        LightningResourceMethodSecurityHolder.setPostResourceMethodSecurity(
                                invocationAttribute.getMethodIdentifierWithActionAndType()
                        );
                    }
                }
            }
        }
    }

}
