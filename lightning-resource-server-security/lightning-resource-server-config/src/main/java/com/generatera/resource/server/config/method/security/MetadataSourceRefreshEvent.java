package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.method.security.entity.ResourceMethodSecurityEntity;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * @author FLJ
 * @date 2023/2/9
 * @time 16:19
 * @Description 事件来源
 */
public class MetadataSourceRefreshEvent extends ApplicationEvent {

    private  boolean forceFlush = false;

    /**
     * 一堆列表 ...
     */
    private final Collection<ResourceMethodSecurityEntity> methodSecurityInfo;

    public MetadataSourceRefreshEvent(Object source) {
        super(source);
        this.methodSecurityInfo = ((Collection<ResourceMethodSecurityEntity>) source);
    }

    /**
     * 强制立即刷新 ... 标志设置 ..
     * @param forceFlush
     */
    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
    }


    public boolean isForceFlush() {
        return forceFlush;
    }

    public Collection<ResourceMethodSecurityEntity> getMethodSecurityInfo() {
        return methodSecurityInfo;
    }
}
