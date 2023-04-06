package com.generatera.resource.server.config.util;

import com.generatera.resource.server.config.LightningResourceMethodSecurityHolder;
import com.generatera.resource.server.config.method.security.LightningInvocationAttribute;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PostInvocationAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;

import java.util.Collection;
import java.util.List;

/**
 * .class
 *
 * @author tianzhangxu
 * @date 2023/4/6 18:47
 */
public class LightningInvocationAttributeUtils {

    public static void evaluateAndSetInvocationResourceMethodSecurity(Collection<ConfigAttribute> attributes) {
        if(attributes != null) {
            for (ConfigAttribute attribute : attributes) {
                if (attribute instanceof LightningInvocationAttribute invocationAttribute) {
                    if (attribute instanceof PostInvocationAttribute) {
                        LightningResourceMethodSecurityHolder.setPostResourceMethodSecurity(
                                invocationAttribute.getMethodIdentifierWithActionAndType()
                        );
                    }
                    else if (attribute instanceof PreInvocationAttribute) {
                        // 设置当前资源方法安全holder
                        LightningResourceMethodSecurityHolder.setPreResourceMethodSecurity(invocationAttribute.getMethodIdentifierWithActionAndType());
                    }
                }
            }
        }
    }

    public static List<ConfigAttribute> unWrapToNativeConfigAttribute(Collection<ConfigAttribute> configAttributes) {
        return configAttributes.stream().map(ele -> {
            if (ele instanceof LightningInvocationAttribute attribute) {
                return attribute.getDelegate();
            }
            return ele;
        }).toList();
    }

}
