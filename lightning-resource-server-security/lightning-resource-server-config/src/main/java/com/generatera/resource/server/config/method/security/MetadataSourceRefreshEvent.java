package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.model.entity.method.security.ResourceMethodSecurityEntity;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/2/9
 * @time 16:19
 * @Description 事件来源
 */
public class MetadataSourceRefreshEvent extends ApplicationEvent {

    /**
     * 一堆列表 ...
     */
    private final Collection<ResourceMethodSecurityEntity> methodSecurityInfo;

    public MetadataSourceRefreshEvent(Object source) {
        super(source);
        this.methodSecurityInfo = ((Collection<ResourceMethodSecurityEntity>) source);
    }

    public Collection<ResourceMethodSecurityEntity> getMethodSecurityInfo() {
        return methodSecurityInfo;
    }
}
