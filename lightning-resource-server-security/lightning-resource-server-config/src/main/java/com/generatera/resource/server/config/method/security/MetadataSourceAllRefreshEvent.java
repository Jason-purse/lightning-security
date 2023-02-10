package com.generatera.resource.server.config.method.security;

import org.springframework.context.ApplicationEvent;
/**
 * @author FLJ
 * @date 2023/2/10
 * @time 10:05
 * @Description 完全刷新 ..
 */
public class MetadataSourceAllRefreshEvent extends ApplicationEvent {
    public MetadataSourceAllRefreshEvent(Object source) {
        super(source);
    }
}
